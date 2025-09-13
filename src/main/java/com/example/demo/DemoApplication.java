package com.example.demo;

import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

import java.io.FileWriter;
import java.io.IOException;

@SpringBootApplication
public class DemoApplication implements CommandLineRunner {

    public static void main(String[] args) {
        SpringApplication.run(DemoApplication.class, args);
    }

    @Override
    public void run(String... args) throws InterruptedException, IOException {
        System.out.println("Приложение запущено. Запускаю потоки...");

        // Создание Markdown отчета
        createMarkdownReport();

        Object lock = new Object();

        // Потоки
        CounterWorker worker = new CounterWorker(1);
        worker.start();

        Thread loggerThread = new Thread(new LoggerTask(lock, 2), "LoggerThread");
        loggerThread.start();

        Thread blockedThread = new Thread(() -> {
            synchronized (lock) {
                System.out.println(Thread.currentThread().getName() + " захватил lock и работает...");
                try {
                    Thread.sleep(2000);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }
        }, "BlockedThread");
        blockedThread.start();

        Thread newThread = new Thread(() -> System.out.println("Этот поток еще не запущен"), "NewThread");
        System.out.println(newThread.getName() + " state: " + newThread.getState());

        // Наблюдение за состояниями потоков
        for (int i = 0; i < 5; i++) {
            System.out.println(worker.getName() + " state: " + worker.getState());
            System.out.println(loggerThread.getName() + " state: " + loggerThread.getState());
            System.out.println(blockedThread.getName() + " state: " + blockedThread.getState());
            Thread.sleep(500);
        }

        // Разблокировка LoggerThread
        synchronized (lock) {
            lock.notify();
        }

        worker.join();
        loggerThread.join();
        blockedThread.join();

        System.out.println(worker.getName() + " final state: " + worker.getState());
        System.out.println(loggerThread.getName() + " final state: " + loggerThread.getState());
        System.out.println(blockedThread.getName() + " final state: " + blockedThread.getState());

        System.out.println("Markdown отчет создан в файле report.md");
    }

    private void createMarkdownReport() throws IOException {
        String report = """
                # Отчет по демонстрации состояний потоков в Java

                ## Цель
                Демонстрация различных состояний потоков (`NEW`, `RUNNABLE`, `WAITING`, `BLOCKED`, `TIMED_WAITING`, `TERMINATED`)
                с использованием трёх потоков.

                ## Используемые потоки
                1. **NewThread** - состояние `NEW` (поток создан, но не запущен)
                2. **CounterWorker** - состояния `RUNNABLE`, `TIMED_WAITING`, `TERMINATED`
                3. **LoggerThread** - состояния `WAITING`, `TERMINATED`
                4. **BlockedThread** - состояния `BLOCKED`, `TIMED_WAITING`, `TERMINATED`

                ## Порядок наблюдения состояний
                - Вывод состояния `NewThread` — `NEW`
                - Запуск остальных потоков
                - Циклический вывод состояний каждые 500 мс
                - Разблокировка `LoggerThread` через `notify()`
                - Завершение всех потоков и вывод их финальных состояний

                ## Выводы
                - Программа демонстрирует все основные состояния потоков в Java.
                - `Thread.getState()` позволяет наблюдать переход потоков между состояниями.
                - `sleep()` демонстрирует `TIMED_WAITING`.
                - Синхронизация через объект `lock` демонстрирует `WAITING` и `BLOCKED`.
                """;

        try (FileWriter writer = new FileWriter("report.md")) {
            writer.write(report);
        }
    }

    // Класс потока через наследование Thread
    static class CounterWorker extends Thread {
        private final int number;

        public CounterWorker(int number) {
            super("CounterWorker");
            this.number = number;
        }

        @Override
        public void run() {
            System.out.println(getName() + " выполняется. Номер: " + number);
            try {
                Thread.sleep(1500); // TIMED_WAITING
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }
    }

    // Класс потока через Runnable
    static class LoggerTask implements Runnable {
        private final Object lock;
        private final int number;

        public LoggerTask(Object lock, int number) {
            this.lock = lock;
            this.number = number;
        }

        @Override
        public void run() {
            synchronized (lock) {
                try {
                    System.out.println(Thread.currentThread().getName() + " переходит в WAITING");
                    lock.wait(); // WAITING
                    System.out.println(Thread.currentThread().getName() + " возобновил работу после wait");
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }
        }
    }
}

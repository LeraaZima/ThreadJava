package com.example.demo;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

public class DemoApplication {
    public static void main(String[] args) {
        // 1) создаём список
        List<Integer> numbers = new ArrayList<>();
        Random random = new Random();
        for (int i = 0; i < 1_000_000; i++) {
            numbers.add(random.nextInt(1_000_000));
        }
        System.out.println("Список создан");

        // 2) последовательный stream
        long t1 = System.currentTimeMillis();
        long sum1 = numbers.stream()
                .filter(n -> n % 2 == 0)
                .mapToLong(n -> n * 2L)
                .sum();
        long t2 = System.currentTimeMillis();
        System.out.println("Stream: сумма=" + sum1 + ", время=" + (t2 - t1) + " ms");

        // 3) параллельный stream
        long t3 = System.currentTimeMillis();
        long sum2 = numbers.parallelStream()
                .filter(n -> n % 2 == 0)
                .mapToLong(n -> n * 2L)
                .sum();
        long t4 = System.currentTimeMillis();
        System.out.println("ParallelStream: сумма=" + sum2 + ", время=" + (t4 - t3) + " ms");
    }
}

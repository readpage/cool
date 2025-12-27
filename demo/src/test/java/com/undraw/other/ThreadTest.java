package com.undraw.other;

import cn.undraw.util.StrUtils;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

public class ThreadTest {

    @Test
    public void test() {
        int i = StrUtils.randomInt(1, 10);
        System.out.println(i);
    }

    @Test
    public void test2() {
        for (int i = 0; i < 5; i++) {
            int a = i;
            Thread thread = new Thread(() -> {
                try {
                    Thread.sleep(1000);
                } catch (InterruptedException e) {
                    throw new RuntimeException(e);
                }
                System.out.println("task " + a);
            });
            thread.start();
        }
        try {
            Thread.sleep(3000);
        } catch (InterruptedException e) {
            throw new RuntimeException(e);
        }
    }

    @Test
    public void test3() {
        // 创建一个固定大小的线程池
        ExecutorService executorService = Executors.newFixedThreadPool(4);

        // 使用Lambda表达式定义任务
        Runnable task1 = () -> {
            System.out.println("Task 1 is running on thread: " + Thread.currentThread().getName());
            try {
                Thread.sleep(2000); // 模拟任务耗时
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
                e.printStackTrace();
            }
            System.out.println("Task 1 is completed");
        };

        Runnable task2 = () -> {
            System.out.println("Task 2 is running on thread: " + Thread.currentThread().getName());
            try {
                Thread.sleep(1000); // 模拟任务耗时
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
                e.printStackTrace();
            }
            System.out.println("Task 2 is completed");
        };

        // 提交任务到线程池
        executorService.submit(task1);
        executorService.submit(task2);

        // 提交更多任务（例如，通过循环）
        for (int i = 3; i <= 5; i++) {
            int taskId = i;
            executorService.submit(() -> {
                System.out.println("Task " + taskId + " is running on thread: " + Thread.currentThread().getName());
                try {
                    Thread.sleep(1000); // 模拟任务耗时
                } catch (InterruptedException e) {
                    Thread.currentThread().interrupt();
                    e.printStackTrace();
                }
                System.out.println("Task " + taskId + " is completed");
            });
        }

        // 关闭线程池（不再接受新任务，但会继续执行已提交的任务）
        executorService.shutdown();
        try {
            // 等待任务完成或超时
            if (!executorService.awaitTermination(60, TimeUnit.SECONDS)) {
                executorService.shutdownNow(); // 强制关闭线程池
            }
        } catch (InterruptedException e) {
            executorService.shutdownNow(); // 强制关闭线程池
            Thread.currentThread().interrupt();
        }
    }

    @Test
    public void test4() {
        ExecutorService executor = Executors.newFixedThreadPool(4);
        Runnable task6 = () -> {
            System.out.println("task 6");
        };
        executor.submit(task6);
        for (int i = 0; i < 5; i++) {
            int taskId = i;
            executor.submit(() -> {
                try {
                    Thread.sleep(1000);
                } catch (InterruptedException e) {
                    throw new RuntimeException(e);
                }
                System.out.println("task " + taskId);
            });
        }
        System.out.println("main task");
        // 关闭线程池（不再接受新任务，但会继续执行已提交的任务）
        executor.shutdown();
        try {
            if (!executor.awaitTermination(Long.MAX_VALUE, TimeUnit.SECONDS)) {
                executor.shutdownNow(); // 尝试停止所有正在执行的任务
            }
        } catch (InterruptedException e) {
            throw new RuntimeException(e);
        }
    }

    @Test
    public void test5() {
        List<Integer> list = new ArrayList<>();
        List<Integer> collect = list.stream().map(o -> o).collect(Collectors.toList());
        System.out.println(collect);
    }
}

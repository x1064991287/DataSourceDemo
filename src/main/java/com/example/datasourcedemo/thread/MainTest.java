package com.example.datasourcedemo.thread;

import java.util.concurrent.ForkJoinPool;

/**
 * @author bxy
 * @date 2024/6/21 09:33:26
 */
public class MainTest {
    public static void main(String[] args) {
        ForkJoinPool forkJoinPool = new ForkJoinPool();
        MyRecursiveTask task = new MyRecursiveTask(10);
        int result = forkJoinPool.invoke(task);
        System.out.println(result);
    }

}

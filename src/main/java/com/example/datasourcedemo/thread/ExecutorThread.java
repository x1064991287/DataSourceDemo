package com.example.datasourcedemo.thread;

import java.util.concurrent.Callable;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

/**
 * ExecutorService类来创建线程
 *
 * @author bxy
 * @date 2024/6/20 16:25:06
 */
public class ExecutorThread {

    public static void main(String[] args) {
        // 创建线程池
        ExecutorService executorService = Executors.newFixedThreadPool(5);
        // 创建线程任务
        Runnable runnable = new Runnable() {
            public void run() {
                System.out.println(Thread.currentThread().getName() + "正在执行Runnable()，无返回值");
            }
        };
        Callable<String> callable = new Callable<String>() {
            public String call() throws Exception {
                return Thread.currentThread().getName() + "正在执行Callable()，有返回值";
            }
        };

        // 提交线程任务
        for (int i = 0; i < 10; i++) {
            executorService.execute(runnable);
        }
        for (int i = 0; i < 10; i++) {
            executorService.submit(callable);
        }
        // 关闭线程池
        executorService.shutdown();
    }

}

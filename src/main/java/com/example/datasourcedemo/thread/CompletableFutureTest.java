package com.example.datasourcedemo.thread;

import java.util.concurrent.CompletableFuture;

/**
 * @author bxy
 * @date 2024/6/21 09:34:49
 */
public class CompletableFutureTest {

    public static void main(String[] args) {
        CompletableFuture<Void> future = CompletableFuture.runAsync(() -> {
            try {
                System.out.println("Thread is running.");
                Thread.sleep(1000);
            } catch (InterruptedException e) {
                throw new RuntimeException(e);
            }
        });

        future.join();
    }

}

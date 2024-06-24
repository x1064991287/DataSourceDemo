package com.example.datasourcedemo.thread;

import java.util.SortedMap;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;

/**
 * 自定义CallAble线程
 *
 * @author bxy
 * @date 2024/6/20 16:19:08
 */
public class CustomerCallAble implements Callable<String> {

    @Override
    public String call() throws Exception {
        return "callAble thread";
    }
    public static void main(String[] args) throws Exception {
        ExecutorService executor = Executors.newSingleThreadExecutor();
        Future<String> future = executor.submit(new CustomerCallAble());
        System.out.println(future.get());
        executor.shutdown();
        System.out.println("main thread");
    }
}

package com.example.datasourcedemo.service;

import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;

/**
 * @author 白秀远
 * @date 2025/7/9 10:57:58
 */
@Service
public class AsyncService {

    @Async("customExecutor")
    public void asyncMethod() {
        System.out.println("执行异步任务，线程：" + Thread.currentThread().getName());
        try {
            Thread.sleep(2000); // 模拟耗时操作
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }
        System.out.println("异步任务完成");
    }
}

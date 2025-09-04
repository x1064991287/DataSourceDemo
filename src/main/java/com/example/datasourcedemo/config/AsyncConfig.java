package com.example.datasourcedemo.config;

/**
 * @author 白秀远
 * @date 2025/7/9 11:06:16
 */
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.scheduling.concurrent.ThreadPoolTaskExecutor;

import java.util.concurrent.Executor;

@Configuration
public class AsyncConfig {

    @Bean(name = "customExecutor")
    public Executor asyncExecutor() {
        ThreadPoolTaskExecutor executor = new ThreadPoolTaskExecutor();
        executor.setCorePoolSize(5);      // 核心线程数
        executor.setMaxPoolSize(10);      // 最大线程数
        executor.setQueueCapacity(100);   // 队列容量
        executor.setThreadNamePrefix("async-");
        executor.initialize();
        return executor;
    }
}


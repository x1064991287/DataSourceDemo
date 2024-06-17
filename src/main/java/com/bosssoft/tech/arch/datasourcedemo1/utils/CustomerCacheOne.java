package com.bosssoft.tech.arch.datasourcedemo1.utils;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.DelayQueue;
import java.util.concurrent.Delayed;
import java.util.concurrent.TimeUnit;

public class CustomerCacheOne {

    // 缓存数据存储
    private static final Map<String, Object> CACHE = new ConcurrentHashMap<>();
    // 过期时间存储
    private static final Map<String, Long> EXPIRATION_TIMES = new ConcurrentHashMap<>();
    // 延迟队列管理过期缓存数据
    private static final DelayQueue<DelayedCacheObject> CLEANUP_QUEUE = new DelayQueue<>();

    static {
        // 启动清理线程
        Thread cleanupThread = new Thread(() -> {
            while (true) {
                try {
                    DelayedCacheObject delayedCacheObject = CLEANUP_QUEUE.take();
                    CACHE.remove(delayedCacheObject.key);
                    EXPIRATION_TIMES.remove(delayedCacheObject.key);
                } catch (InterruptedException e) {
                    Thread.currentThread().interrupt();
                }
            }
        });
        cleanupThread.setDaemon(true);
        cleanupThread.start();
    }

    public static Object get(String key) {
        if (!EXPIRATION_TIMES.containsKey(key) || EXPIRATION_TIMES.get(key) > System.currentTimeMillis()) {
            return CACHE.get(key);
        } else {
            remove(key);
            return null;
        }
    }

    public static void set(String key, Object value, long ttl, TimeUnit timeUnit) {
        long expirationTime = System.currentTimeMillis() + timeUnit.toMillis(ttl);
        CACHE.put(key, value);
        EXPIRATION_TIMES.put(key, expirationTime);
        CLEANUP_QUEUE.put(new DelayedCacheObject(key, expirationTime));
    }

    public static void remove(String key) {
        CACHE.remove(key);
        EXPIRATION_TIMES.remove(key);
    }

    public static void clear() {
        CACHE.clear();
        EXPIRATION_TIMES.clear();
        CLEANUP_QUEUE.clear();
    }

    // 用于延迟队列的缓存对象
    private static class DelayedCacheObject implements Delayed {
        private final String key;
        private final long expirationTime;

        public DelayedCacheObject(String key, long expirationTime) {
            this.key = key;
            this.expirationTime = expirationTime;
        }

        @Override
        public long getDelay(TimeUnit unit) {
            return unit.convert(expirationTime - System.currentTimeMillis(), TimeUnit.MILLISECONDS);
        }

        @Override
        public int compareTo(Delayed o) {
            if (this.expirationTime < ((DelayedCacheObject) o).expirationTime) {
                return -1;
            }
            if (this.expirationTime > ((DelayedCacheObject) o).expirationTime) {
                return 1;
            }
            return 0;
        }
    }
}

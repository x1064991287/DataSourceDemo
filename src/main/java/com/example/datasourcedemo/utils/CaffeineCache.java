package com.example.datasourcedemo.utils;

import com.github.benmanes.caffeine.cache.Cache;
import com.github.benmanes.caffeine.cache.Caffeine;

import java.util.concurrent.TimeUnit;

/**
 * wasm缓存类
 *
 * @author bxy
 * @date 2024/7/4 15:24:39
 */
public class CaffeineCache {

    private static final CaffeineCache instance = new CaffeineCache();
    // 创建caffeine缓存

    private static Cache<String, String> cache = Caffeine.newBuilder()
                                                                     .expireAfterWrite(5, TimeUnit.MINUTES)
                                                                     .maximumSize(50)
                                                                     .build();

    private CaffeineCache() {
    }

    public static CaffeineCache getInstance() {
        return instance;
    }

    public static void putCache(String key, String value) {
        cache.put(key, value);
    }

    public static String getCache(String key) {
        return cache.getIfPresent(key);
    }

    public static void removeCache(String key) {
        cache.invalidate(key);
    }

    public static void clearAll() {
        cache.invalidateAll();
    }

    public static int getCacheSize() {
        return (int) cache.estimatedSize();
    }

    public static String getRandomCache() {
        return cache.asMap().values().stream().findFirst().orElse(null);
    }

}

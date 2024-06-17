package com.example.datasourcedemo.utils;

import java.util.Map;

/**
 * 自定义缓存
 *
 * @author bxy
 * @date 2024/6/14 17:30:39
 */
public class CustomerCache {

    private static final Map<String, Object> CACHE = new java.util.concurrent.ConcurrentHashMap<>();

    public static Object get(String key) {
        return CACHE.get(key);
    }

    public static void set(String key, Object value) {
        CACHE.put(key, value);
    }

    public static void remove(String key) {
        CACHE.remove(key);
    }

    public static void clear() {
        CACHE.clear();
    }

}

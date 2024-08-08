package com.example.datasourcedemo.utils;

import java.util.HashMap;
import java.util.Map;

/**
 *
 * @author bxy
 * @date 2024/8/1 10:58:34
 */
public class GetValueIgnoreCase {

    public static void main(String[] args) {

        Map<String, String> map = new HashMap<>();
        map.put("Name", "John");
        map.put("AGE", "30");
        map.put("city", "New York");

        System.out.println("Original map: " + map);

        // 测试我们的方法
        System.out.println("Value for 'name': " + getIgnoreCase(map, "name"));
        System.out.println("Value for 'AGE': " + getIgnoreCase(map, "AGE"));
        System.out.println("Value for 'City': " + getIgnoreCase(map, "City"));
        System.out.println("Value for 'country': " + getIgnoreCase(map, "country"));
    }
    public static String getIgnoreCase(Map<String, String> map, String key) {
        for (Map.Entry<String, String> entry : map.entrySet()) {
            if (entry.getKey().equalsIgnoreCase(key)) {
                return entry.getValue();
            }
        }
        return null; // 如果没有找到匹配的键，返回null
    }
}

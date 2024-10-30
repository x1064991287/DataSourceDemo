package com.example.datasourcedemo.test;

import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 *  测试Map中的merge方法 计算List中每个元素出现的次数
 * @author bxy
 * @date 2024/10/30 15:57:11
 */
public class TestMapMerge {

    public static void main(String[] args) {
        test1();
        test2();
        test3();
        test4();
        test5();
        test6();
        test7();
    }

    /**
     * 计算list中每个元素出现的次数
     */
    public static void test1() {
        System.out.println("计算list中每个元素出现的次数");
        List<String> ctList = Arrays.asList("a", "b", "c", "a", "b", "a");
        Map<String, Integer> map = new HashMap<>();
        for (String item : ctList) {
            map.merge(item, 1, Integer::sum);
        }
        System.out.println(map);
        System.out.println("==============================");
    }

    /**
     * 字符串拼接
     */
    public static void test2() {
        System.out.println("字符串拼接");
        Map<String, String> map = new HashMap<>();
        map.put("a", "a");
        map.put("b", "b");
        map.merge("a", "b", (v1, v2) -> v1 + " and " + v2);
        System.out.println(map);
        System.out.println("==============================");
    }

    /**
     * 数值计算
     */
    public static void test3() {
        System.out.println("数值计算");
        Map<String, Integer> scores = new HashMap<>();
        scores.put("Alice", 90);
        scores.put("Bob", 85);
        scores.put("Charlie", 95);
        scores.merge("Alice", 10, (oldScore, newScore) -> oldScore + newScore);
        System.out.println(scores);
        System.out.println("==============================");
    }

    /**
     * 取最大值
     */
    public static void test4() {
        System.out.println("取最大值");
        Map<String, Integer> scores = new HashMap<>();
        scores.put("Alice", 90);
        scores.put("Bob", 85);
        scores.put("Charlie", 95);
        scores.merge("Alice", 100, (oldScore, newScore) -> Math.max(oldScore, newScore));
        System.out.println(scores);
        System.out.println("==============================");
    }

    /**
     * 取最小值
     */
    public static void test5() {
        System.out.println("取最小值");
        Map<String, Integer> scores = new HashMap<>();
        scores.put("Alice", 90);
        scores.put("Bob", 85);
        scores.put("Charlie", 95);
        scores.merge("Alice", 10, (oldScore, newScore) -> Math.min(oldScore, newScore));
        System.out.println(scores);
        System.out.println("==============================");
    }

    /**
     * 移除元素
     */
    public static void test6() {
        System.out.println("移除元素");
        Map<String, Integer> scores = new HashMap<>();
        scores.put("Alice", 90);
        scores.put("Bob", 85);
        scores.put("Charlie", 95);
        scores.put("Charlie1", 950);
        scores.merge("Alice", 10, (oldScore, newScore) -> oldScore < 100 ? null : newScore);
        System.out.println(scores);
        System.out.println("==============================");
    }

    /**
     * 更新元素
     */
    public static void test7() {
        System.out.println("更新元素");
        Map<String, String> scores = new HashMap<>();
        scores.put("A", "a");
        scores.put("B", "b");
        scores.put("C", "c");
        scores.merge("A", "aa", (oldScore, newScore) -> newScore);
        System.out.println(scores);
        System.out.println("==============================");
    }


}

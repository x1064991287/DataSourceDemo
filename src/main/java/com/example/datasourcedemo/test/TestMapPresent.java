package com.example.datasourcedemo.test;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * @author bxy
 * @date 2024/10/30 15:59:05
 */
public class TestMapPresent {

    public static void main(String[] args) {

        test1();
        test2();
        test3();
    }

    /**
     * computeIfPresent测试，键存在则执行操作
     */
    public static void test1() {
        System.out.println("computeIfPresent测试，键存在则执行操作");
        List<String> list = new ArrayList<>();
        list.add("a");
        list.add("b");
        list.add("c");
        Map<String, Integer> map = new HashMap<>();
        map.put("a", 1);
        map.put("b", 1);
        System.out.println("执行操作前");
        System.out.println(map);
        //map中存在键，则value+1
        for (String s : list) {
            map.computeIfPresent(s, (k, v) -> v + 1);
        }
        System.out.println("执行操作后");
        System.out.println(map);
        System.out.println("==============================");
    }

    /**
     * computeIfAbsent测试，键不存在则添加键并执行操作
     */

    public static void test2() {
        System.out.println("computeIfAbsent测试，键不存在则添加键并执行操作");
        Map<String, Integer> map = new HashMap<>();
        map.put("a", 1);
        map.put("b", 1);
        System.out.println("执行操作前");
        System.out.println(map);
        //map中不存在键，则添加键，并设置value为1
        map.computeIfAbsent("c", k -> 1);
        System.out.println("执行操作后");
        System.out.println(map);
        System.out.println("==============================");
    }

    /**
     *  compute测试 key不存在则添加键，存在则执行操作
     *  key存在时，value为null，则删除键
     *
     */
    public static void test3() {
        System.out.println("compute测试");
        Map<String, Integer> map = new HashMap<>();
        map.put("a", 1);
        map.put("b", 1);
        System.out.println("执行操作前");
        System.out.println(map);
        //map中不存在键，则添加键，并设置value为1
        map.compute("c",(k,v)->10);
        //map中存在键，则value为null，则删除键
        map.compute("a",(k,v)->null);
        System.out.println("执行操作后");
        System.out.println(map);
        System.out.println("==============================");
    }

}

package com.example.datasourcedemo.thread;

import java.util.SortedMap;
import java.util.concurrent.Callable;

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
        CustomerCallAble customerCallAble = new CustomerCallAble();
        String call = customerCallAble.call();
        System.out.println(call);
        System.out.println("主线程执行");
    }
}

package com.example.datasourcedemo.thread;

/**
 * 多线程
 *
 * @author bxy
 * @date 2024/6/20 16:14:23
 */
public class CustomerRunnableThread implements Runnable {

    public static void main(String[] args) {
        //执行线程
        CustomerRunnableThread customerRunnableThread = new CustomerRunnableThread();
        Thread thread = new Thread(customerRunnableThread);
        thread.start();
        System.out.println("主线程执行");
    }

    @Override
    public void run() {
        System.out.println("线程执行");
    }
}

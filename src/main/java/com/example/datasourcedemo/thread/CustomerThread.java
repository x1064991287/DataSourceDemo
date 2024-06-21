package com.example.datasourcedemo.thread;

/**
 * 自定义继承Thread
 *
 * @author bxy
 * @date 2024/6/20 16:23:11
 */
public class CustomerThread extends Thread{
    private String name;

    public CustomerThread(String name) {
        this.name = name;
    }

    @Override
    public void run() {
        System.out.println(name + "线程开始执行");
        try {
            Thread.sleep(1000);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
        System.out.println(name + "线程执行结束");
    }

    public static void main(String[] args) {
        CustomerThread customerThread1 = new CustomerThread("线程1");
        customerThread1.start();
        System.out.println("主线程执行结束");
    }
}

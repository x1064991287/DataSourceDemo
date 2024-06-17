package com.example.datasourcedemo.service;

import java.util.ServiceLoader;

/**
 *  SPI 测试
 * @author bxy
 * @date 2024/6/14 11:42:35
 */
public class TestMain {

    public static void main(String[] args) {
        ServiceLoader<InterfaceA> serviceLoader = ServiceLoader.load(InterfaceA.class);
        for (InterfaceA interfaceA : serviceLoader) {
            interfaceA.print();
        }

    }

}

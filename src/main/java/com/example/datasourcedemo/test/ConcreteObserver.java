package com.example.datasourcedemo.test;

/**
 * @author 白秀远
 * @date 2024/12/26 13:45:44
 */
class ConcreteObserver implements Observer {
    private String name;

    public ConcreteObserver(String name) {
        this.name = name;
    }

    @Override
    public void update(String message) {
        System.out.println(name + " 收到消息: " + message);
    }
}


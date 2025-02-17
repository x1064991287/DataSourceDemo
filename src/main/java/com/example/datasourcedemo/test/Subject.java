package com.example.datasourcedemo.test;

import java.util.ArrayList;
import java.util.List;

/**
 * @author 白秀远
 * @date 2024/12/26 13:44:45
 */
// 主题（Subject）类
class Subject {
    private List<Observer> observers = new ArrayList<>();

    // 添加观察者
    public void attach(Observer observer) {
        observers.add(observer);
    }

    // 移除观察者
    public void detach(Observer observer) {
        observers.remove(observer);
    }

    // 通知所有观察者
    public void notifyObservers(String message) {
        for (Observer observer : observers) {
            observer.update(message);
        }
    }
}

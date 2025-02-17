package com.example.datasourcedemo.test;

/**
 * @author 白秀远
 * @date 2024/12/26 13:46:04
 */
public class ObserverPatternDemo {
    public static void main(String[] args) {
        // 创建主题对象
        Subject subject = new Subject();

        // 创建观察者对象
        ConcreteObserver observer1 = new ConcreteObserver("观察者1");
        ConcreteObserver observer2 = new ConcreteObserver("观察者2");

        // 将观察者附加到主题
        subject.attach(observer1);
        subject.attach(observer2);

        // 主题状态变化，通知所有观察者
        subject.notifyObservers("主题状态发生变化！");

        // 移除一个观察者
        subject.detach(observer1);

        // 再次通知观察者
        subject.notifyObservers("主题状态再次变化！");
    }
}

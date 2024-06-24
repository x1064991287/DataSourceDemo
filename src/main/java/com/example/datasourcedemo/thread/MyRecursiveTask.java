package com.example.datasourcedemo.thread;

/**
 * @author bxy
 * @date 2024/6/21 09:27:25
 */

import java.util.concurrent.RecursiveTask;

class MyRecursiveTask extends RecursiveTask<Integer> {

    private final int workload;

    MyRecursiveTask(int workload) {
        this.workload = workload;
    }

    @Override
    protected Integer compute() {
        if (workload > 1) {
            int split = workload / 2;
            MyRecursiveTask subtask1 = new MyRecursiveTask(split);
            MyRecursiveTask subtask2 = new MyRecursiveTask(workload - split);

            subtask1.fork();
            subtask2.fork();

            return subtask1.join() + subtask2.join();
        } else {
            return 1;
        }
    }

}

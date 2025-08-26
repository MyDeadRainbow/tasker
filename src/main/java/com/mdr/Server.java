package com.mdr;

import com.mdr.task.TaskLoader;
import com.mdr.task.TestTask;

public class Server {
    public static void main(String[] args) {
        System.out.println("Hello world!");

        startScheduler();
        startListener();

    }

    static void startScheduler() {
        TaskScheduler scheduler = new TaskScheduler();
        new TaskLoader().loadTasks().forEach(scheduler::scheduleTask);
    }

    static void startListener() {
        new CommandListener().start();
    }
}

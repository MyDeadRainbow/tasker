package com.mdr;

import com.mdr.cli.CommandListener;
import com.mdr.task.TaskLoader;

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

package com.mdr;

import com.mdr.cli.CommandListener;
import com.mdr.task.TaskLoader;

public class Server {
    public static void main(String[] args) {
        System.out.println("Hello world!");

        startScheduler();
        startListener();

        // TaskScheduler.getInstance().close();
    }

    static void startScheduler() {
        TaskLoader.loadTasks().forEach(TaskScheduler::scheduleTask);
    }

    static void startListener() {
        new ServerArgumentListener().start();
    }
}

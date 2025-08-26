package com.mdr;

import java.io.BufferedReader;
import java.io.Closeable;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.ServerSocket;
import java.net.Socket;
import java.time.Duration;
import java.time.LocalDateTime;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

import com.mdr.task.Task;
import com.mdr.task.TestTask;

public class Server {
    public static void main(String[] args) {
        System.out.println("Hello world!");

        Server server = new Server();
        server.startScheduler();
        server.startListener();

    }

    void startScheduler() {
        Scheduler scheduler = new Scheduler();
        scheduler.scheduleTask(new TestTask());
    }

    void startListener() {
        new CommandListener().start();
    }
}

class Scheduler {

    ScheduledExecutorService scheduler = Executors.newSingleThreadScheduledExecutor();
    ExecutorService executor = Executors.newVirtualThreadPerTaskExecutor();

    Scheduler() {

    }

    public void scheduleTask(Task task) {
        LocalDateTime startTime = task.getStartTime();
        long initialDelay = Duration.between(LocalDateTime.now(), startTime).toSeconds();
        scheduler.scheduleAtFixedRate(() -> {
            executor.execute(task::execute);
        }, initialDelay, task.getInterval(), TimeUnit.SECONDS);
    }

    public void close() {
        try {
            scheduler.shutdown();
            executor.shutdown();
        } catch (Exception e) {
            e.printStackTrace();
        }

    }
}

class CommandListener {
    public void start() {
        Thread.ofPlatform().name("app-listener").start(() -> {
            try (ServerSocket serverSocket = new ServerSocket(0)) {
                System.out.println("App listening on port: " + serverSocket.getLocalPort());
                Props.APP_PORT.setValue(String.valueOf(serverSocket.getLocalPort()));
                while (true) {
                    Socket clientSocket = serverSocket.accept();
                    BufferedReader clientInput = new BufferedReader(new InputStreamReader(clientSocket.getInputStream()));
                    String command = clientInput.readLine();
                    System.out.println("Received command: " + command);

                    // Handle client cli commands
                }
            } catch (IOException e) {
                e.printStackTrace();
            }
        });
    }
}
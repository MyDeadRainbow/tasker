package com.mdr.cli;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.Arrays;
import java.util.List;
import java.util.function.Consumer;

import com.mdr.Log;
import com.mdr.Props;
import com.mdr.TaskScheduler;
import com.mdr.task.TaskLoader;
import com.mdr.task.TaskRecord;

public class CommandListener {
    private static final Log log = Log.getLogger(CommandListener.class);
    private static ServerSocket serverSocket;
    private static Socket clientSocket;
    public void start() {
        Thread.ofPlatform().name("app-listener").start(() -> {
            try {
                serverSocket = new ServerSocket(0);
                log.info("App listening on port: " + serverSocket.getLocalPort());
                Props.APP_PORT.set(String.valueOf(serverSocket.getLocalPort()));
                do  {
                    clientSocket = serverSocket.accept();                    
                    if (clientSocket != null && clientSocket.isClosed()) {
                        clientSocket = null;
                        continue;
                    }
                    BufferedReader clientInput = new BufferedReader(new InputStreamReader(clientSocket.getInputStream()));
                    String command = clientInput.readLine();
                    log.info("Received command: " + command);
                    String[] parts = command.split(" ");                    
                    Commands cmd = Commands.getByName(parts[0]);
                    if (cmd == null) {
                        log.warning("Unknown command: " + parts[0]);
                    } else {
                        if (parts.length > 1) {
                            cmd.execute(Arrays.copyOfRange(parts, 1, parts.length));
                        } else {
                            cmd.execute(new String[]{});
                        }
                    }
                    // Handle client cli commands
                } while (clientSocket != null && !clientSocket.isClosed() && !serverSocket.isClosed());
            } catch (IOException e) {
                log.severe("Error occurred: " + e.getMessage(), e);
            } finally {
                if (serverSocket != null && !serverSocket.isClosed()) {
                    try {
                        serverSocket.close();
                    } catch (IOException e) {
                        log.severe("Error occurred when closing server socket: " + e.getMessage(), e);
                    }
                }
                if (clientSocket != null && !clientSocket.isClosed()) {
                    try {
                        clientSocket.close();
                    } catch (IOException e) {
                        log.severe("Error occurred when closing client socket: " + e.getMessage(), e);
                    }
                }
            }
        });
    }

    
    public enum Commands {
        START(v -> {
            System.out.println("Starting...");
        }),
        STOP(v -> {
            log.info("Stopping...");
            try (BufferedWriter clientOutput = new BufferedWriter(new OutputStreamWriter(clientSocket.getOutputStream()))) {
                clientOutput.write("Stopping...");
                clientOutput.newLine();
                clientOutput.flush();
                if (clientSocket != null && !clientSocket.isClosed()) {
                    try {
                        clientSocket.close();
                    } catch (IOException e) {
                        log.severe("Error occurred when closing client socket: " + e.getMessage(), e);
                    }
                }
                if (serverSocket != null && !serverSocket.isClosed()) {
                    try {
                        serverSocket.close();
                    } catch (IOException e) {
                        log.severe("Error occurred when closing server socket: " + e.getMessage(), e);
                    }
                }
            } catch (IOException e) {
                log.severe("Error occurred when stopping: " + e.getMessage(), e);
            } finally {
                TaskScheduler.close();
            }
            

        }),
        ADD(value -> {
            log.info("Adding: " + Arrays.toString(value));

            String jarPath = value[0];
            String overrideStartTime;
            Integer overrideInterval;
            if (value.length > 1) {
                overrideStartTime = value[1];
            } else {
                overrideStartTime = null;
            }
            if (value.length > 2) {
                overrideInterval = Integer.parseInt(value[2]);
            } else {
                overrideInterval = null;
            }
            // Add the task to the TaskLoader
            List<TaskRecord> tasks = TaskLoader.addTasksFromJar(jarPath, overrideStartTime, overrideInterval);
            if (tasks != null) {
                tasks.forEach(task -> {
                    TaskScheduler.scheduleTask(task);
                    log.info("Task added successfully: " + task);
                });
            } else {
                log.warning("Failed to add task from JAR: " + jarPath);
            }
        }),
        REMOVE(value -> {
            log.info("Removing: " + Arrays.toString(value));
            String jarPath = value[0];
            TaskScheduler.removeTask(jarPath);
        })
        ;

        Consumer<String[]> command;

        Commands(Consumer<String[]> command) {
            this.command = command;
        }

        public void execute(String[] value) {
            command.accept(value);
        }

        public static Commands getByName(String name) {
            for (Commands command : Commands.values()) {
                if (command.name().equalsIgnoreCase(name)) {
                    return command;
                }
            }
            return null;
        }
    }
}

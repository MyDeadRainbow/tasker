package com.mdr.cli.parser;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.Arrays;
import java.util.List;
import java.util.function.BiConsumer;
import java.util.logging.Level;
import java.util.logging.Logger;

import com.mdr.Props;
import com.mdr.TaskScheduler;
import com.mdr.task.TaskLoader;
import com.mdr.task.framework.TaskRecord;
import com.mdr.util.function.TriConsumer;

public enum ServerArguments implements Argument {
    STOP(new String[] { "-stop" }, 1, ActionPriority.EXCLUSIVE, (value, log) -> {
        log.info("Stopping...");
        try (BufferedWriter clientOutput = new BufferedWriter(
                new OutputStreamWriter(ServerArgumentListener.clientSocket.getOutputStream()))) {
            clientOutput.write("Stopping...");
            clientOutput.newLine();
            clientOutput.flush();
            
        } catch (IOException e) {
            log.log(Level.SEVERE, "Error occurred when stopping: " + e.getMessage(), e);
        } finally {
            if (ServerArgumentListener.clientSocket != null && !ServerArgumentListener.clientSocket.isClosed()) {
                try {
                    ServerArgumentListener.clientSocket.close();
                } catch (IOException e) {
                    log.log(Level.SEVERE, "Error occurred when closing client socket: " + e.getMessage(), e);
                }
            }
            if (ServerArgumentListener.serverSocket != null && !ServerArgumentListener.serverSocket.isClosed()) {
                try {
                    ServerArgumentListener.serverSocket.close();
                } catch (IOException e) {
                    log.log(Level.SEVERE, "Error occurred when closing server socket: " + e.getMessage(), e);
                }
            }
            TaskScheduler.close();
        }

    }, (errorMessage, log, throwable) -> {
        log.log(Level.SEVERE, "Error occurred while processing stop command: " + errorMessage, throwable);
    }),
    //TODO: This needs to accept task names or class paths per start time and interval overrides. Current implementation will use the same values for all the tasks in the jar
    ADD(new String[] { "-add" }, 3, ActionPriority.EXCLUSIVE, (value, log) -> {
        String[] args = value.split(" ");
        String jarPath = args[1];
        String overrideStartTime;
        Integer overrideInterval;
        if (args.length > 2) {
            overrideStartTime = args[2];
        } else {
            overrideStartTime = null;
        }
        if (args.length > 3) {
            overrideInterval = Integer.parseInt(args[3]);
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
    }, (errorMessage, log, throwable) -> {
        log.log(Level.SEVERE, "Error occurred while processing add command: " + errorMessage, throwable);
    }),
    REMOVE(new String[] { "-remove" }, 1, ActionPriority.EXCLUSIVE, (value, log) -> {
        String[] args = value.split(" ");
        String jarPath = args[1];
        TaskScheduler.removeTask(jarPath);
    }, (errorMessage, log, throwable) -> {
        log.log(Level.SEVERE, "Error occurred while processing remove command: " + errorMessage, throwable);
    }),
    NONE(new String[] {}, 1, ActionPriority.LOW, (value, log) -> {
        log.info("No command provided. Use -help for assistance.");
    }, (errorMessage, log, throwable) -> {
        log.log(Level.SEVERE, "Error occurred while processing empty command: " + errorMessage, throwable);
    });

    private static final Logger log = Logger.getLogger(ServerArguments.class.getName());
    private final String[] identifiers;
    private final int parts;
    private final ActionPriority priority;
    public final BiConsumer<String, Logger> process;
    public final TriConsumer<String, Logger, Throwable> onError;

    ServerArguments(String[] identifiers, int parts, ActionPriority priority, BiConsumer<String, Logger> process,
            TriConsumer<String, Logger, Throwable> onError) {
        this.identifiers = identifiers;
        this.parts = parts;
        this.priority = priority;
        this.process = process;
        this.onError = onError;
    }

    public String getFirstIdentifier() {
        return identifiers.length > 0 ? identifiers[0] : "";
    }

    @Override
    public String[] getIdentifiers() {
        return identifiers;
    }

    @Override
    public int getParts() {
        return parts;
    }

    @Override
    public ActionPriority getPriority() {
        return priority;
    }

    @Override
    public void process(String value) {
        log.info("Processing Argument[" + this + "]: '" + value + "'");
        try {
            process.accept(value, log);
        } catch (Throwable e) {
            onError.accept(e.getMessage(), log, e);
        }
    }

    @Override
    public void onError(String errorMessage, Throwable throwable) {
        onError.accept(errorMessage, log, throwable);
    }

    public static Argument parse(String input) {
        String[] parts = input.split(" ");
        Argument argument = NONE;
        for (String part : parts) {
            for (ServerArguments arg : ServerArguments.values()) {
                if (Arrays.asList(arg.getIdentifiers()).contains(part)) {
                    argument = arg;
                    break;
                }
            }
        }
        return argument;
    }

    public static ServerArgumentListener listener() {
        return ServerArgumentListener.INSTANCE;
    }

    public static enum ServerArgumentListener {
        INSTANCE;

        private static final Logger log = Logger.getLogger(ServerArgumentListener.class.getName());
        private static ServerSocket serverSocket;
        private static Socket clientSocket;

        private ServerArgumentListener() {
        }

        public void start() {
            Thread.ofPlatform().name("app-listener").start(() -> {
                try {
                    serverSocket = new ServerSocket(0);
                    log.info("App listening on port: " + serverSocket.getLocalPort());
                    Props.APP_PORT.set(String.valueOf(serverSocket.getLocalPort()));
                    do {
                        clientSocket = serverSocket.accept();
                        if (clientSocket != null && clientSocket.isClosed()) {
                            clientSocket = null;
                            continue;
                        }
                        BufferedReader clientInput = new BufferedReader(
                                new InputStreamReader(clientSocket.getInputStream()));
                        String input = clientInput.readLine();
                        log.info("Received argument: " + input);
                        Argument arg = ServerArguments.parse(input);
                        arg.process(input);
                    } while (clientSocket != null && !clientSocket.isClosed() && !serverSocket.isClosed());
                } catch (IOException e) {
                    
                    log.log(Level.SEVERE, "Error occurred: " + e.getMessage(), e);
                } finally {
                    if (serverSocket != null && !serverSocket.isClosed()) {
                        try {
                            serverSocket.close();
                        } catch (IOException e) {
                            log.log(Level.SEVERE, "Error occurred when closing server socket: " + e.getMessage(), e);
                        }
                    }
                    if (clientSocket != null && !clientSocket.isClosed()) {
                        try {
                            clientSocket.close();
                        } catch (IOException e) {
                            log.log(Level.SEVERE, "Error occurred when closing client socket: " + e.getMessage(), e);
                        }
                    }
                }
            });
        }
    }
}

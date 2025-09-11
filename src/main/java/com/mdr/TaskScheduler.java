package com.mdr;

import java.time.Duration;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.TimeUnit;
import java.util.logging.Logger;

import com.mdr.task.framework.TaskRecord;

public class TaskScheduler {
    private static final Logger log = Logger.getLogger(TaskScheduler.class.getName());

    private static ScheduledExecutorService scheduler = Executors.newScheduledThreadPool(1);
    private static ExecutorService executor = Executors.newVirtualThreadPerTaskExecutor();

    // private static final TaskScheduler instance = new TaskScheduler();
    private static final Map<TaskRecord, ScheduledFuture<?>> scheduledTasks = new HashMap<>();

    // public static TaskScheduler getInstance() {
    //     return instance;
    // }

    private TaskScheduler() {
    }

    public static void scheduleTask(TaskRecord task) {
        if (scheduledTasks.containsKey(task)) {
            log.warning("Task is already scheduled: " + task.classPath());
            return;
        }
        LocalDateTime startTime = task.startTime();
        long initialDelay = Duration.between(LocalTime.now(), startTime.toLocalTime()).toSeconds();
        ScheduledFuture<?> future = scheduler.scheduleAtFixedRate(() -> {
            // log.info("Executing task: " + task.classPath());
            // task.task().run();
            executor.execute(
                task.task()
            );
        }, initialDelay, task.interval(), TimeUnit.SECONDS);
        scheduledTasks.put(task, future);

    }

    // public static void scheduleNewTask(TaskRecord task) {
    //     getInstance().scheduleTask(task);
    // }

    public static void removeTask(String jarPath) {
        scheduledTasks.entrySet().stream()
                .filter(entry -> entry.getKey().jarPath().equals(jarPath))
                .findFirst()
                .ifPresent(entry -> {
                    entry.getValue().cancel(true);
                    scheduledTasks.remove(entry.getKey());
                });
    }

    // public static void removeScheduledTask(String jarPath) {
    //     getInstance().removeTask(jarPath);
    // }


    public static void close() {
        scheduledTasks.values().forEach(future -> future.cancel(true));
        scheduler.shutdown(); // Disable new tasks from being submitted
        try {
            // Wait a while for existing tasks to terminate
            if (!scheduler.awaitTermination(1, TimeUnit.SECONDS)) {
            scheduler.shutdownNow(); // Cancel currently executing tasks
            // Wait a while for tasks to respond to being cancelled
            if (!scheduler.awaitTermination(1, TimeUnit.SECONDS))
                System.err.println("scheduler did not terminate");
            }
        } catch (InterruptedException ie) {
            // (Re-)Cancel if current thread also interrupted
            scheduler.shutdownNow();
            // Preserve interrupt status
            Thread.currentThread().interrupt();
        }
        // try {
        //     scheduler.shutdownNow();            
        //     // if (!scheduler.isTerminated()) {
        //         // scheduler = null;
        //     // }
        //     // scheduler.notifyAll();
        //     Thread.currentThread().interrupt();
        //     // executor.shutdownNow();
        // } catch (Exception e) {
        //     log.severe("Error occurred when closing TaskScheduler: " + e.getMessage(), e);
        // }

    }
}

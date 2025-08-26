package com.mdr;

import java.time.Duration;
import java.time.LocalDateTime;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

import com.mdr.task.Task;

public class TaskScheduler {
    ScheduledExecutorService scheduler = Executors.newSingleThreadScheduledExecutor();
    ExecutorService executor = Executors.newVirtualThreadPerTaskExecutor();

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

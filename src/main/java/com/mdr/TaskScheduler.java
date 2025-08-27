package com.mdr;

import java.time.Duration;
import java.time.LocalDateTime;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;
import java.util.logging.Logger;

import com.mdr.task.TaskRecord;

public class TaskScheduler {
    private static final Logger log = LogFactory.getLogger(TaskScheduler.class);

    ScheduledExecutorService scheduler = Executors.newSingleThreadScheduledExecutor();
    ExecutorService executor = Executors.newVirtualThreadPerTaskExecutor();

    public void scheduleTask(TaskRecord task) {
        LocalDateTime startTime = task.startTime();
        long initialDelay = Duration.between(LocalDateTime.now(), startTime).toSeconds();
        scheduler.scheduleAtFixedRate(() -> {
            log.info("Executing task: " + task.name());
            executor.execute(task.task());
        }, initialDelay, task.interval(), TimeUnit.SECONDS);
    }

    public void close() {
        try {
            scheduler.shutdown();
            executor.shutdown();
        } catch (Exception e) {
            log.severe("Error occurred when closing TaskScheduler: " + e.getMessage());
        }

    }
}

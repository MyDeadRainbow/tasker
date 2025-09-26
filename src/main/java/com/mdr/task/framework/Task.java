package com.mdr.task.framework;

import java.time.LocalDateTime;
import java.util.logging.Level;
import java.util.logging.Logger;

public abstract class Task {
    public abstract void execute();

    private LocalDateTime startTime;
    private int interval;

    protected Task(LocalDateTime startTime, int interval) throws TaskInitializationException {
        if (startTime == null) {
            throw new TaskInitializationException("Start time cannot be null");
        }
        if (interval <= 0) {
            throw new TaskInitializationException("Interval must be positive");
        }
        this.startTime = startTime;
        this.interval = interval;
    }

    public LocalDateTime getStartTime() {
        return startTime;
    }

    public int getInterval() {
        return interval;
    }

    public final Runnable getRun() {
        return () -> {
            Logger log = Logger.getLogger(this.getClass().getName());
            try {
                log.log(Level.INFO, "Executing task: " + this.getClass().getName());
                // System.out.println("Executing task: " + this.getClass().getName());
                execute();
            } catch (Throwable e) {
                log.log(Level.SEVERE, "Error occurred when executing task", e);
            }
        };
    }
}

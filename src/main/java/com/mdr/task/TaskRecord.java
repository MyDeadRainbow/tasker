package com.mdr.task;

import java.time.LocalDateTime;

public record TaskRecord(String name, LocalDateTime startTime, int interval, Runnable task) {
}

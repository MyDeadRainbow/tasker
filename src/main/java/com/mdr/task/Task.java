package com.mdr.task;

import java.time.LocalDateTime;

public interface Task {
    void execute();
    LocalDateTime getStartTime();
    int getInterval();
}

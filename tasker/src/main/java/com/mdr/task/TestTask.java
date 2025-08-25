package com.mdr.task;

import java.time.LocalDateTime;

public class TestTask implements Task {

    @Override
    public LocalDateTime getStartTime() {
        return LocalDateTime.now().plusSeconds(5);
    }

    @Override
    public int getInterval() {
        return 10;
    }

    @Override
    public void execute() {
        System.out.println("Executing test task");
    }
}

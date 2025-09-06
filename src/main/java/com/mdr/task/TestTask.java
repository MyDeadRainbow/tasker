package com.mdr.task;

import java.time.LocalDateTime;

import com.mdr.Props;
import com.mdr.task.framework.Task;
import com.mdr.task.framework.TaskMetadata;

@TaskMetadata(name = "Test Task", startTime = "2023-10-01 10:00:00", interval = 10)
public class TestTask implements Task {

    @Override
    public void execute() {
        System.out.println("Executing Test Task at "
                + LocalDateTime.now().format(Props.DATE_FORMAT.get()));
    }

    @Override
    public String toString() {
        TaskMetadata task = this.getClass().getAnnotation(TaskMetadata.class);
        if (task != null) {
            return "TestTask{" +
                    "name='" + task.name() + '\'' +
                    ", startTime='" + task.startTime() + '\'' +
                    ", interval=" + task.interval() +
                    '}';
        }
        return "TestTask";
    }
}

package com.mdr;

import java.time.LocalDateTime;

import com.mdr.task.framework.Task;
import com.mdr.task.framework.TaskMetadata;

@TaskMetadata(startTime = "2023-10-01 10:00:00", interval = 10)
public class TestTask extends Task {

    public TestTask(LocalDateTime startTime, int interval) {
        super(startTime, interval);
    }    

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
                    ", startTime='" + task.startTime() + '\'' +
                    ", interval=" + task.interval() +
                    '}';
        }
        return "TestTask";
    }
}

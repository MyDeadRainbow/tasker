package com.mdr.task;

import java.time.LocalDateTime;

import com.mdr.Props;
import com.mdr.task.annotations.Executer;
import com.mdr.task.annotations.Task;

@Task(name = "Test Task", startTime = "2023-10-01 10:00:00", interval = 10)
public class TestTask {

    @Executer
    public void execute(String str) {
        System.out.println("Executing Test Task at "
                + LocalDateTime.now().format(Props.DATE_FORMAT.get()));
    }

    @Override
    public String toString() {
        Task task = this.getClass().getAnnotation(Task.class);
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

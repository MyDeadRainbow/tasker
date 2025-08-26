package com.mdr.task;

import java.time.LocalDateTime;

// @Retention(RetentionPolicy.RUNTIME)
// @Target(ElementType.TYPE)
// public @interface Task {
//     // String name();
//     // String description() default "";
//     // void execute();
//     // LocalDateTime getStartTime();
//     // int getInterval();
// }

public interface Task {
    void execute();
    LocalDateTime getStartTime();
    int getInterval();
    String toString();
}

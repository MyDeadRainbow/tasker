package com.mdr.task.annotations;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

@Target(ElementType.TYPE)
@Retention(RetentionPolicy.RUNTIME)
public @interface Task {
    String name();
    String startTime();
    int interval();
    String version() default "1.0";
    String description() default "";
    String author() default "";
}

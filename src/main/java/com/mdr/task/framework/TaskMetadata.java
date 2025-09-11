package com.mdr.task.framework;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Annotation to provide default metadata for task classes.
 * startTime and interval can be overriden from the command line.
 * 
 */
@Target(ElementType.TYPE)
@Retention(RetentionPolicy.RUNTIME)
public @interface TaskMetadata {
    /**
     * The start time of the task.
     * yyyy-MM-dd HH:mm:ss
     */
    String startTime();

    /**
     * The interval at which the task should run.
     * in seconds
     */
    int interval();

    // /**
    //  * The name of the task.
    //  * null/default is the fully qualified class name
    //  */
    // String name();

    /**
     * The version of the task.
     * default is "1.0"
     */
    String version() default "1.0";

    /**
     * The description of the task.
     * default is an empty string
     */
    String description() default "";

    /**
     * The author of the task.
     * default is an empty string
     */
    String author() default "";
}

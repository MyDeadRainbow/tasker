package com.mdr.task.annotations;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Must satisfy the Runnable contract.
 */
@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.METHOD)
public @interface Executer {
    // Class<?> returnType() default void.class; // Expected return type
    // Class<?>[] paramTypes() default {}; // Expected parameter types
}

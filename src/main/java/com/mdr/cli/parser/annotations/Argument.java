package com.mdr.cli.parser.annotations;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

import com.mdr.cli.parser.ActionPriority;

@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.TYPE)
public @interface Argument {
    String[] identifiers();
    // void process(String value);
    int parts();
    int order();
}

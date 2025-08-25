package com.mdr.cli.parser;

import java.util.function.Function;

public interface Argument<T> {
    String getPrefix();
    // String getValue();
    Function<String, T> getProcess();
    T process(String value);
}

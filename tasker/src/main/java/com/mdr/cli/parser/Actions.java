package com.mdr.cli.parser;

import java.util.function.Consumer;
import java.util.function.Function;

import javax.lang.model.type.NullType;

public enum Actions implements Argument<NullType> {
    ADD("-add", value -> {
        System.out.println(value);
        return null;
    });

    private final String prefix;
    private final Function<String, NullType> process;

    Actions(String prefix, Function<String, NullType> process) {
        this.prefix = prefix;
        this.process = process;
    }

    @Override
    public String getPrefix() {
        return prefix;
    }

    // @Override
    // public String getValue() {
    //     return value;
    // }

    @Override
    public Function<String, NullType> getProcess() {
        return process;
    }

    @Override
    public NullType process(String value) {
        return process.apply(value);
    }
}

package com.mdr.cli.parser;

import java.util.function.Function;

import javax.lang.model.type.NullType;

public record ActionArgument(String prefix, String value, Function<String, NullType> process) implements Argument<NullType> {

    @Override
    public String getPrefix() {
        return prefix;
    }

    // @Override
    // public String getValue() {
    //     return value;
    // }

    @Override
    public Function getProcess() {
        // TODO Auto-generated method stub
        throw new UnsupportedOperationException("Unimplemented method 'getProcess'");
    }

    @Override
    public NullType process(String value) {
        // TODO Auto-generated method stub
        throw new UnsupportedOperationException("Unimplemented method 'process'");
    }


}


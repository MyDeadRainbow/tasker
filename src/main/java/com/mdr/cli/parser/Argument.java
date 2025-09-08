package com.mdr.cli.parser;

public interface Argument {
    String[] getIdentifiers();
    void process(String value);
    void onError(String errorMessage, Throwable throwable);
    int getParts();
    ActionPriority getPriority();

    
}

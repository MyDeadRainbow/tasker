package com.mdr.cli.parser;

public interface Argument {
    String[] getIdentifiers();
    void process(String value);
    void onError(String errorMessage);
    int getParts();
    ActionPriority getPriority();

    
}

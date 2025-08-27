package com.mdr.cli.parser;

public interface Argument {
    String[] getIdentifiers();
    void process(String value);
    int getParts();
    ActionPriority getPriority();

    
}

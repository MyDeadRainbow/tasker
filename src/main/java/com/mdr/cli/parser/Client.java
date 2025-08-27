package com.mdr.cli.parser;

import java.util.List;

public class Client {
    public static void main(String[] args) {
        args = List.of("-start").toArray(String[]::new);
        
        Actions action = Actions.getByPrefix(args[0]);
        action.process(null);

        
    }
}

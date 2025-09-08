package com.mdr.cli;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;

import com.mdr.cli.parser.ActionPriority;
import com.mdr.cli.parser.ClientArguments;

public class Client {
    public static void main(String[] args) {
        args = List.of(
        // "-start"
        // , 
        // "-stop"
        //,
        "-add", "G:\\Projects\\email-task\\target\\email-task-1.0-SNAPSHOT-fat.jar"//<jarPath> [overrideStartTime] [overrideInterval]"
        ).toArray(String[]::new);

        Map<ClientArguments, String> actions = parseArgs(args);
        actions.forEach((action, value) -> {
            System.out.println("Action: " + action.name());
            action.process(value);
        });
        
    }

    private static Map<ClientArguments, String> parseArgs(String[] args) {
        Map<ClientArguments, String> actions = new TreeMap<>((a, b) -> {
            if (a.getPriority() == ActionPriority.EXCLUSIVE || b.getPriority() == ActionPriority.EXCLUSIVE) {
                if (a.getPriority() == ActionPriority.EXCLUSIVE && b.getPriority() == ActionPriority.EXCLUSIVE) {
                    return 0;
                } else if (a.getPriority() == ActionPriority.EXCLUSIVE) {
                    return -1;
                } else {
                    return 1;
                }
            }
            if (a.getPriority() == b.getPriority()) {
                return a.compareTo(b);
            }
            return a.getPriority().compareTo(b.getPriority());
        });
        for (int i = 0; i < args.length;) {
            ClientArguments action = ClientArguments.getByPrefix(args[i]);
            if (action != null) {
                StringBuilder value = new StringBuilder();
                for (int j = 1; j < action.getParts(); j++) {
                    if (i + j < args.length) {
                        value.append(args[i + j]);
                        if (j < action.getParts() - 1) {
                            value.append(" ");
                        }
                    }
                }
                if (actions.containsKey(action)) {
                    action.onError.accept("Duplicate argument: " + Arrays.toString(action.getIdentifiers()) + " " + value
                            + ". Only the first occurrence will be processed.");
                }
                actions.putIfAbsent(action, value.toString().trim());
                i += action.getParts();
            } else {
                i++;
            }
        }
        if (actions.isEmpty()) {
            actions.put(ClientArguments.EMPTY, null);
        }
        return actions;
    }

}

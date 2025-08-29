package com.mdr.cli.parser;

import java.io.BufferedWriter;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.net.Socket;
import java.nio.file.Paths;
import java.util.Arrays;
import java.util.Properties;
import java.util.function.Consumer;
import java.util.function.Function;

import javax.lang.model.type.NullType;

import com.mdr.Props;
import com.mdr.cli.CommandListener;

public enum Actions implements Argument {
    START_SERVER(new String[] { "-start" }, 1, ActionPriority.EXCLUSIVE, value -> {
        ProcessBuilder processBuilder = new ProcessBuilder("serv.bat");//new ProcessBuilder("java", "-jar", Props.SERVER_PATH.get());
        processBuilder.directory(Paths.get("").toAbsolutePath().toFile());
        processBuilder.redirectErrorStream(true);
        try {
            Process process = processBuilder.start();
            Props.SERVER_PID.set(String.valueOf(process.pid()));

        } catch (IOException e) {
            e.printStackTrace();
        }
    }, (errorMessage) -> {
        System.out.println("Error occurred while processing start command: " + errorMessage);
    }),
    STOP_SERVER(new String[] { "-stop" }, 1, ActionPriority.EXCLUSIVE, value -> {
        sendCommand(CommandListener.Commands.STOP + " " + value);
    }, (errorMessage) -> {
        System.out.println("Error occurred while processing stop command: " + errorMessage);
    }),
    ADD(new String[] { "-add", "-a" }, 2, ActionPriority.LOW, value -> {
        System.out.println(value);
        Actions.sendCommand(CommandListener.Commands.ADD + " " + value);
    }, (errorMessage) -> {
        System.out.println("Error occurred while processing add command: " + errorMessage);
    }),
    REMOVE(new String[] { "-remove", "-rm" }, 2, ActionPriority.LOW, value -> {
        System.out.println(value);
        Actions.sendCommand(CommandListener.Commands.REMOVE + " " + value);
    }, (errorMessage) -> {
        System.out.println("Error occurred while processing remove command: " + errorMessage);
    }),
    HELP(new String[] { "-help", "-h" }, 1, ActionPriority.HIGH, value -> {
        System.out.println("Help command invoked");
    }, (errorMessage) -> {
        System.out.println("Error occurred while processing help command: " + errorMessage);
    }),
    EMPTY(new String[] {}, 1, ActionPriority.LOW, value -> {
        System.out.println("No command provided. Use -help for assistance.");
    }, (errorMessage) -> {
        System.out.println("Error occurred while processing empty command: " + errorMessage);
    });

    private final String[] identifiers;
    public final Consumer<String> process;
    public final Consumer<String> onError;
    private final ActionPriority priority;
    private final int parts;

    Actions(String[] identifiers, int parts, ActionPriority priority, Consumer<String> process,
            Consumer<String> onError) {
        this.identifiers = identifiers;
        this.parts = parts;
        this.process = process;
        this.priority = priority;
        this.onError = onError;
    }

    @Override
    public String[] getIdentifiers() {
        return identifiers;
    }

    @Override
    public int getParts() {
        return parts;
    }

    @Override
    public ActionPriority getPriority() {
        return priority;
    }

    @Override
    public void process(String value) {
        process.accept(value);
    }

    // @Override
    // public boolean equals(Object obj) {
    // if (this == obj) return true;
    // if (obj == null || getClass() != obj.getClass()) return false;
    // Actions other = (Actions) obj;
    // return Arrays.equals(identifiers, other.identifiers);
    // }

    public static Actions getByPrefix(String prefix) {
        for (Actions action : Actions.values()) {
            if (Arrays.stream(action.getIdentifiers())
                    .anyMatch(identifier -> identifier.equals(prefix))) {
                return action;
            }
        }
        return null; // or throw an exception if preferred
    }

    private static void sendCommand(String command) {
        try (Socket clientSocket = new Socket("localhost", Props.APP_PORT.get())) {
            BufferedWriter clientOutput = new BufferedWriter(new OutputStreamWriter(clientSocket.getOutputStream()));
            System.out.println("Sending command: " + command);
            clientOutput.write(command);
            clientOutput.newLine();
            clientOutput.flush();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}

enum ActionProperties {
    START_SERVER(),
    STOP_SERVER(),
    ADD(),
    REMOVE();

    private final Properties properties;

    ActionProperties() {
        properties = new Properties();
    }

    public String get() {
        return properties.getProperty(this.name());
    }

    public void set(String value) {
        properties.setProperty(this.name(), value);
    }
}

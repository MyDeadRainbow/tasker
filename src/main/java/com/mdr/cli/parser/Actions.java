package com.mdr.cli.parser;

import java.io.BufferedWriter;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.net.Socket;
import java.util.function.Consumer;
import java.util.function.Function;

import javax.lang.model.type.NullType;

import com.mdr.Props;

public enum Actions implements Argument<NullType> {
    START_SERVER("-start", value -> {
        ProcessBuilder processBuilder = new ProcessBuilder("java", "-jar", Props.SERVER_PATH.get());
        // processBuilder.redirectErrorStream(true);
        // processBuilder.
        try {
            Process process = processBuilder.start();
            Props.SERVER_PID.set(String.valueOf(process.pid()));

        } catch (IOException e) {
            e.printStackTrace();
        }
        return null;
    }),
    STOP_SERVER("-stop", value -> {
        System.out.println(value);
        return null;
    }),
    ADD("-add", value -> {
        System.out.println(value);
        return null;
    }),
    REMOVE("-remove", value -> {
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

    public static Actions getByPrefix(String prefix) {
        for (Actions action : Actions.values()) {
            if (action.getPrefix().equals(prefix)) {
                return action;
            }
        }
        return null; // or throw an exception if preferred
    }

    private void sendCommand(String command) {
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

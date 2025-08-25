package com.mdr.cli.parser;

import java.io.BufferedWriter;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.net.Socket;
import java.util.List;

import com.mdr.Props;

public class Client {
    public static void main(String[] args) {
        args = List.of("hello!").toArray(String[]::new);
        try (Socket clientSocket = new Socket("localhost", Props.APP_PORT.getInt())) {
            BufferedWriter clientOutput = new BufferedWriter(new OutputStreamWriter(clientSocket.getOutputStream()));
            String command = String.join(" ", args);
            System.out.println("Sending command: " + command);
            clientOutput.write(command);
            clientOutput.newLine();
            clientOutput.flush();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}

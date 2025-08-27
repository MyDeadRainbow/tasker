package com.mdr;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.logging.Logger;

public class CommandListener {
    private static final Logger log = LogFactory.getLogger(CommandListener.class);
    public void start() {
        Thread.ofPlatform().name("app-listener").start(() -> {
            try (ServerSocket serverSocket = new ServerSocket(0)) {
                log.info("App listening on port: " + serverSocket.getLocalPort());
                Props.APP_PORT.set(String.valueOf(serverSocket.getLocalPort()));
                while (true) {
                    Socket clientSocket = serverSocket.accept();
                    BufferedReader clientInput = new BufferedReader(new InputStreamReader(clientSocket.getInputStream()));
                    String command = clientInput.readLine();
                    System.out.println("Received command: " + command);

                    // Handle client cli commands
                }
            } catch (IOException e) {
                log.severe("Error occurred: " + e.getMessage());
            }
        });
    }
}

package com.mdr;

import java.util.logging.ConsoleHandler;
import java.util.logging.FileHandler;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.logging.SimpleFormatter;

public class Log {
    public static void setup() {
        try {
            System.setProperty("java.util.logging.SimpleFormatter.format", "[%1$tF %1$tT] %2$s%4$s: %5$s%n%6$s%n");
            Logger root = Logger.getLogger("");
            root.setLevel(Level.ALL);

            FileHandler fileHandler = new FileHandler("tasker-output.log", true);
            fileHandler.setFormatter(new SimpleFormatter());
            root.addHandler(fileHandler);

            ConsoleHandler consoleHandler = new ConsoleHandler();
            consoleHandler.setFormatter(new SimpleFormatter());
            root.addHandler(consoleHandler);
        } catch (Exception e) {
            System.err.println("Failed to set up log file handler: " + e.getMessage());
        }
    }

}

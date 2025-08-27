package com.mdr;

import java.util.logging.FileHandler;
import java.util.logging.Logger;
import java.util.logging.SimpleFormatter;

public class LogFactory {

    private static FileHandler fileHandler = null;
    static {
        try {
            fileHandler = new FileHandler("tasker-output.log", true);
            fileHandler.setFormatter(new SimpleFormatter());
        } catch (Exception e) {
            System.err.println("Failed to set up log file handler: " + e.getMessage());
        }
    }

    public static Logger getLogger(Class<?> clazz) {
        return getLogger(clazz.getName());
    }

    public static Logger getLogger(String name) {
        Logger log = Logger.getLogger(name);
        log.addHandler(fileHandler);
        log.setUseParentHandlers(false);
        return log;
    }

}

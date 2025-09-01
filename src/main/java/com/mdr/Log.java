package com.mdr;

import java.util.Arrays;
import java.util.logging.FileHandler;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.logging.SimpleFormatter;
import java.util.stream.Collectors;

public class Log {

    private Logger logger;
    private static FileHandler fileHandler = null;
    static {
        try {
            fileHandler = new FileHandler("tasker-output.log", true);
            fileHandler.setFormatter(new SimpleFormatter());
        } catch (Exception e) {
            System.err.println("Failed to set up log file handler: " + e.getMessage());
        }
    }

    private Log(String name) {
        logger = Logger.getLogger(name);
    }

    public static Log getLogger(Class<?> clazz) {
        return getLogger(clazz.getName());
    }

    public static Log getLogger(String name) {
        Log log = new Log(name);
        log.logger.addHandler(fileHandler);
        log.logger.setUseParentHandlers(false);
        return log;
    }

    private String getStackTraceString(Throwable throwable) {
        return Arrays.stream(throwable.getStackTrace()).map(StackTraceElement::toString)
                .collect(Collectors.joining("\n"));
    }

    public void info(String msg) {
        logger.info(msg);
        System.out.println(msg);
    }

    public void info(String msg, Throwable throwable) {
        logger.log(Level.INFO, msg, throwable);
        System.out.println(msg + "\n" + getStackTraceString(throwable));
    }

    public void severe(String msg) {
        logger.severe(msg);
        System.out.println(msg);
    }

    public void severe(String msg, Throwable throwable) {
        logger.log(Level.SEVERE, msg, throwable);
        System.out.println(msg + "\n" + getStackTraceString(throwable));
    }

    public void warning(String msg) {
        logger.warning(msg);
        System.out.println(msg);
    }

    public void warning(String msg, Throwable throwable) {
        logger.log(Level.WARNING, msg, throwable);
        System.out.println(msg + "\n" + getStackTraceString(throwable));
    }

}

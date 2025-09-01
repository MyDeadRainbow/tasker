package com.mdr;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.nio.file.Paths;
import java.time.format.DateTimeFormatter;
import java.util.Properties;
import java.util.function.Function;
import java.util.logging.Logger;

public class Props<T> {
    public static final Props<Integer> APP_PORT = new Props<>("server.port",
            "12980", Integer::valueOf);
    public static final Props<DateTimeFormatter> DATE_FORMAT = new Props<>("date.format",
            "yyyy-MM-dd HH:mm:ss", DateTimeFormatter::ofPattern);
    public static final Props<String> SERVER_PATH = new Props<>("server.path",
            Paths.get("server.jar").toAbsolutePath().toString(), Function.identity());
    public static final Props<String> SERVER_PID = new Props<>("server.pid",
            "", Function.identity());

    private static final Log log = Log.getLogger(Props.class);

    private static final String PROPS_FILE = "application.properties";
    private static final String PATH = Paths.get("").toAbsolutePath().toString();
    private static final Properties properties;
    static {
        properties = new Properties();
        File file = new File(PATH, PROPS_FILE);
        if (!file.exists()) {
            try {
                file.createNewFile();
            } catch (IOException e) {
                log.severe("Error occurred when creating properties file: " + e.getMessage(), e);
            }
        }
        try (FileInputStream fis = new FileInputStream(file)) {
            properties.load(fis);
        } catch (IOException e) {
            log.severe("Error occurred when loading properties file: " + e.getMessage(), e);
        }
    }
    private final String key;
    private final String defaultValue;
    private final Function<String, T> parser;

    private Props(String key, String defaultValue, Function<String, T> parser) {
        this.key = key;
        this.defaultValue = defaultValue;
        this.parser = parser;
    }

    public String getKey() {
        return key;
    }

    public String getDefaultValue() {
        return defaultValue;
    }

    public T get() {
        String value = properties.getProperty(key);
        if (value == null) {
            value = defaultValue;
            set(value);
        }
        return parser.apply(value);
    }

    // public int getInt() {
    // return Integer.parseInt(properties.getProperty(key, defaultValue));
    // }

    public void set(String value) {
        properties.setProperty(key, value);
        Thread.ofVirtual().start(() -> saveProperties());
    }

    private void saveProperties() {
        try (FileOutputStream fos = new FileOutputStream(new File(PATH, PROPS_FILE))) {
            properties.store(fos, null);
        } catch (IOException e) {
            log.severe("Error occurred when saving properties file: " + e.getMessage(), e);
        }
    }
}

interface Gen<T> {

    public abstract T getValue();
}
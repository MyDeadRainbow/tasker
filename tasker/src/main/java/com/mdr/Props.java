package com.mdr;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.nio.file.Paths;
import java.util.Properties;

public enum Props {
    APP_PORT("server.port", "12980"),
    // DATABASE_URL("database.url", "jdbc:mysql://localhost:3306/mydb"),
    // DATABASE_USER("database.user", "user"),
    // DATABASE_PASSWORD("database.password", "password");
    ;

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
                e.printStackTrace();
            }
        }
        try (FileInputStream fis = new FileInputStream(file)) {
            properties.load(fis);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
    private final String key;
    private final String defaultValue;

    Props(String key, String defaultValue) {
        this.key = key;
        this.defaultValue = defaultValue;
    }

    public String getKey() {
        return key;
    }

    public String getDefaultValue() {
        return defaultValue;
    }

    public String getValue() {
        return properties.getProperty(key, defaultValue);
    }

    public int getInt() {
        return Integer.parseInt(properties.getProperty(key, defaultValue));
    }

    public void setValue(String value) {
        properties.setProperty(key, value);
        Thread.ofVirtual().start(() -> saveProperties());
    }

    private void saveProperties() {
        try (FileOutputStream fos = new FileOutputStream(new File(PATH, PROPS_FILE))) {
            properties.store(fos, null);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}

package com.mdr.task;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.URL;
import java.net.URLClassLoader;
import java.nio.file.Paths;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Enumeration;
import java.util.List;
import java.util.Set;
import java.util.jar.JarEntry;
import java.util.jar.JarFile;
import java.util.logging.Level;
import java.util.logging.Logger;

import org.json.JSONException;
import org.json.JSONObject;
import org.reflections.Reflections;
import org.reflections.scanners.Scanners;
import org.reflections.util.ClasspathHelper;
import org.reflections.util.ConfigurationBuilder;

import com.mdr.Props;
import com.mdr.task.framework.Task;
import com.mdr.task.framework.TaskMetadata;
import com.mdr.task.framework.TaskRecord;

public class TaskLoader {
    private static final Logger log = Logger.getLogger(TaskLoader.class.getName());

    private static final String JSON = "tasks.json";
    private static final String TASK_FOLDER = "tasks";

    private static JSONObject json;

    public static List<TaskRecord> loadTasks() {
        List<TaskRecord> taskRecords = new ArrayList<>();
        try {
            loadJson();
            json.getJSONArray("tasks").forEach(item -> {
                JSONObject taskJson = (JSONObject) item;
                TaskRecord taskRecord = reflectTaskFromJson(taskJson);
                taskRecords.add(taskRecord);
            });
        } catch (JSONException e) {
            log.log(Level.SEVERE, "Error occurred when loading tasks from JSON: " + e.getMessage(), e);
        } catch (IOException e) {
            log.log(Level.SEVERE, "Error occurred when loading tasks from JSON: " + e.getMessage(), e);
        }
        return taskRecords;
    }

    private static JSONObject loadJson() throws IOException {
        if (json == null) {
            File file = new File(Paths.get(TASK_FOLDER, JSON).toUri());
            if (!file.exists()) {
                log.info("Creating new JSON file: " + file.getAbsolutePath());
                file.getParentFile().mkdirs();
                file.createNewFile();
            }
            try (BufferedReader reader = new BufferedReader(new InputStreamReader(
                    new FileInputStream(file)))) {

                String jsonString = reader.lines().reduce("", (acc, line) -> {
                    if (line.trim().length() > 0) {
                        acc += line.trim();
                    }
                    return acc;
                });
                if (jsonString.isEmpty()) {
                    jsonString = "{\"tasks\":[]}";
                    json = new JSONObject(jsonString);
                    saveJson();
                } else {
                    json = new JSONObject(jsonString);
                }
            }
        }
        return json;
    }

    private static void saveJson() {
        Thread.ofVirtual().start(() -> saveJsonSync());
    }

    private static void saveJsonSync() {
        if (json == null) {
            return;
        }
        try (BufferedWriter writer = new BufferedWriter(new FileWriter(Paths.get(TASK_FOLDER, JSON).toFile()))) {
            writer.write(json.toString());
        } catch (IOException e) {
            log.log(Level.SEVERE, "Error occurred when saving JSON file: " + e.getMessage(), e);
        }

    }

    public static TaskRecord reflectTaskFromJson(JSONObject taskJson) {
        String jarPath = taskJson.getString("jarPath");
        String classPath = taskJson.getString("classPath");
        String startTime = taskJson.getString("startTime");
        int interval = taskJson.getInt("interval");

        File jarFile = new File(jarPath);
        URLClassLoader classLoader = null;
        TaskRecord task = null;
        try {
            URL jarUrl = jarFile.toURI().toURL();

            // Create a URLClassLoader to load the JAR
            classLoader = new URLClassLoader(new URL[] { jarUrl }, ClassLoader.getSystemClassLoader());

            try (JarFile jar = new JarFile(jarFile)) {
                Enumeration<JarEntry> entries = jar.entries();
                while (entries.hasMoreElements()) {
                    JarEntry entry = entries.nextElement();
                    if (entry.getName().startsWith("META-INF"))
                        continue;
                    if (entry.getName().endsWith(".class")) {
                        try {
                            String className = entry.getName().replace("/", ".").replace(".class", "");
                            classLoader.loadClass(className);
                        } catch (ClassNotFoundException | NoClassDefFoundError e) {
                            log.log(Level.SEVERE, "Class not found: " + e.getMessage(), e);
                        }
                    }
                }
            }
            // Configure Reflections to scan the JAR
            // Reflections reflections = new Reflections(new ConfigurationBuilder()
            //         .setUrls(ClasspathHelper.forClassLoader(classLoader))
            //         .setScanners(Scanners.SubTypes));

            // Set<String> taskClassNames = reflections.getStore().get("SubTypes")
            //         .get("com.mdr.task.framework.Task");
            // for (String string : taskClassNames) {
                Class<?> clazz = classLoader.loadClass(classPath);
                TaskMetadata taskAnnotation = clazz.getAnnotation(TaskMetadata.class);
                task = createTaskRecord(jarPath, clazz, taskAnnotation,
                        startTime, interval);
            // }
        } catch (Exception e) {
            log.log(Level.SEVERE, "Error occurred when loading tasks: " + e.getMessage(), e);
        } finally {
            if (classLoader != null) {
                try {
                    classLoader.close();
                } catch (IOException e) {
                    log.log(Level.SEVERE, "Error occurred when closing class loader: " + e.getMessage(), e);
                }
            }
        }
        return task;
    }

    public static List<TaskRecord> addTasksFromJar(String jarPath, String overrideStartTime, Integer overrideInterval) {
        File jarFile = new File(jarPath);

        //TODO: decide if i want a plugin folder or just store the path in json
        //Testing is easier when we just store the path in json
        // try {
        //     jarFile = Files.copy(jarFile.toPath(), Paths.get(TASK_FOLDER, jarFile.getName()),
        //             StandardCopyOption.REPLACE_EXISTING).toFile();
        // } catch (IOException e) {
        //     log.log(Level.SEVERE, "Error occurred when copying JAR file: " + e.getMessage(), e);
        //     return null;
        // }
        URLClassLoader classLoader = null;
        List<TaskRecord> tasks = new ArrayList<>();
        try {
            URL jarUrl = jarFile.toURI().toURL();

            // Create a URLClassLoader to load the JAR
            classLoader = new URLClassLoader(new URL[] { jarUrl }, ClassLoader.getSystemClassLoader());

            try (JarFile jar = new JarFile(jarFile)) {
                Enumeration<JarEntry> entries = jar.entries();
                while (entries.hasMoreElements()) {
                    JarEntry entry = entries.nextElement();
                    if (entry.getName().startsWith("META-INF"))
                        continue;
                    if (entry.getName().endsWith(".class")) {
                        try {
                            String className = entry.getName().replace("/", ".").replace(".class", "");
                            classLoader.loadClass(className);
                        } catch (ClassNotFoundException | NoClassDefFoundError e) {
                            log.log(Level.SEVERE, "Class not found: " + e.getMessage(), e);
                        }
                    }
                }
            }
            // Configure Reflections to scan the JAR
            Reflections reflections = new Reflections(new ConfigurationBuilder()
                    .setUrls(ClasspathHelper.forClassLoader(classLoader))
                    .setScanners(Scanners.SubTypes));

            Set<String> taskClassNames = reflections.getStore().get("SubTypes")
                    .get("com.mdr.task.framework.Task");
            for (String string : taskClassNames) {
                Class<?> clazz = classLoader.loadClass(string);
                TaskMetadata taskAnnotation = clazz.getAnnotation(TaskMetadata.class);
                TaskRecord task = createTaskRecord(jarPath, clazz, taskAnnotation,
                        overrideStartTime, overrideInterval);

                
            }
        } catch (Exception e) {
            log.log(Level.SEVERE, "Error occurred when loading tasks: " + e.getMessage(), e);
        } finally {
            if (classLoader != null) {
                try {
                    classLoader.close();
                } catch (IOException e) {
                    log.log(Level.SEVERE, "Error occurred when closing class loader: " + e.getMessage(), e);
                }
            }
        }
        try {
            loadJson();
            for (TaskRecord task : tasks) {
                json.getJSONArray("tasks").put(task.toJson());
            }
            saveJson();
        } catch (IOException e) {
            log.log(Level.SEVERE, "Error occurred when saving task to JSON: " + e.getMessage(), e);
        }

        return tasks;
    }

    private static TaskRecord createTaskRecord(String jarPath, Class<?> clazz, TaskMetadata taskAnnotation,
            String overrideStartTime, Integer overrideInterval) {

        final Task instance;
        try {
            LocalDateTime startTime = LocalDateTime.parse(overrideStartTime != null
                        ? overrideStartTime
                        : taskAnnotation.startTime(),
                        Props.DATE_FORMAT.get());

            int interval = overrideInterval != null ? overrideInterval : taskAnnotation.interval();
            instance = (Task) clazz.getDeclaredConstructor().newInstance(startTime, interval);
        } catch (Exception e) {
            log.log(Level.SEVERE, "Error occurred when creating task instance", e);
            return null;
        }
        return new TaskRecord(
                jarPath,
                instance.getClass().getName(),
                instance.getStartTime(),
                instance.getInterval(),
                instance.run()
                );
    }
}

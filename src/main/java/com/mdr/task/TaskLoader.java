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
import java.util.ArrayList;
import java.util.List;
import java.util.Set;

import org.json.JSONObject;
import org.reflections.Reflections;
import org.reflections.scanners.Scanners;
import org.reflections.util.ClasspathHelper;
import org.reflections.util.ConfigurationBuilder;

public class TaskLoader {

    private static final String JSON = "tasks.json";
    private static final String TASK_FOLDER = "tasks";

    private JSONObject json;

    public static void main(String[] args) throws Exception {
        new TaskLoader().loadTasks();
    }

    List<String> loadTasksPathsFromJson() {
        List<String> taskPaths = new ArrayList<>();
        BufferedReader bufferedReader = null;
        try {
            File file = new File(Paths.get(TASK_FOLDER, JSON).toUri());
            if (!file.exists()) {
                file.createNewFile();
            }
            InputStreamReader reader = new InputStreamReader(
                    new FileInputStream(file));

            bufferedReader = new BufferedReader(reader);
            String jsonString = bufferedReader.lines().reduce("", (acc, line) -> {
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

            json.getJSONArray("tasks").forEach(item -> {
                String taskPath = item.toString();
                taskPaths.add(taskPath);
            });
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            if (bufferedReader != null) {
                try {
                    bufferedReader.close();
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        }
        return taskPaths;
    }

    private void saveJson() {
        Thread.ofVirtual().start(() -> saveJsonSync());
    }

    private void saveJsonSync() {
        if (json == null) {
            return;
        }
        try (BufferedWriter writer = new BufferedWriter(new FileWriter(Paths.get(TASK_FOLDER, JSON).toFile()))) {
            writer.write(json.toString());
        } catch (IOException e) {
            e.printStackTrace();
        }

    }

    private List<String> loadTaskPathsFromFolder() {
        List<String> taskPaths = new ArrayList<>();
        File folder = new File(TASK_FOLDER);
        File[] files = folder.listFiles((dir, name) -> name.endsWith(".jar"));
        for (File file : files) {
            taskPaths.add(file.getAbsolutePath());
        }
        return taskPaths;
    }

    public List<Task> loadTasks() {
        List<String> taskPaths = new ArrayList<>();
        taskPaths.addAll(loadTasksPathsFromJson());
        taskPaths.addAll(loadTaskPathsFromFolder());

        List<Task> tasks = new ArrayList<>();
        for (String string : taskPaths) {

            // 1. Obtain the JAR file path
            File jarFile = new File(string);
            URLClassLoader classLoader = null;
            try {
                URL jarUrl = jarFile.toURI().toURL();

                // Create a URLClassLoader to load the JAR
                classLoader = new URLClassLoader(new URL[] { jarUrl }, ClassLoader.getSystemClassLoader());

                // Configure Reflections to scan the JAR
                Reflections reflections = new Reflections(new ConfigurationBuilder()
                        .setUrls(ClasspathHelper.forClassLoader(classLoader))
                        .setScanners(Scanners.SubTypes));

                // Get all classes annotated with MyAnnotation.class
                Set<Class<? extends Task>> taskSubtypes = reflections.getSubTypesOf(Task.class);

                // Process the found classes
                for (Class<? extends Task> clazz : taskSubtypes) {
                    Task task = clazz.getDeclaredConstructor().newInstance();
                    tasks.add(task);
                }
            } catch (Exception e) {
                e.printStackTrace();
            } finally {
                if (classLoader != null) {
                    try {
                        classLoader.close();
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                }
            }
        }

        return tasks;
    }
}

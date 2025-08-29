package com.mdr.task;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStreamReader;
import java.lang.reflect.Method;
import java.net.URL;
import java.net.URLClassLoader;
import java.nio.file.Paths;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Set;
import java.util.logging.Logger;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONML;
import org.json.JSONObject;
import org.reflections.Reflections;
import org.reflections.scanners.Scanners;
import org.reflections.util.ClasspathHelper;
import org.reflections.util.ConfigurationBuilder;

import com.mdr.LogFactory;
import com.mdr.Props;
import com.mdr.task.annotations.Executer;
import com.mdr.task.annotations.Task;

public class TaskLoader {
    private static final Logger log = LogFactory.getLogger(TaskLoader.class);

    private static final String JSON = "tasks.json";
    private static final String TASK_FOLDER = "tasks";

    private JSONObject json;

    public List<TaskRecord> loadTasks() {
        List<TaskRecord> taskRecords = new ArrayList<>();
        try {
            loadJson();
            json.getJSONArray("tasks").forEach(item -> {
                JSONObject taskJson = (JSONObject) item;
                TaskRecord taskRecord = TaskRecord.fromJson(taskJson);
                taskRecords.add(taskRecord);
            });
        } catch (JSONException e) {
            log.severe("Error occurred when loading tasks from JSON: " + e.getMessage());
        } catch (IOException e) {
            log.severe("Error occurred when loading tasks from JSON: " + e.getMessage());
        }
        return taskRecords;
    }

    private JSONObject loadJson() throws IOException {
        if (json == null) {
            File file = new File(Paths.get(TASK_FOLDER, JSON).toUri());
            if (!file.exists()) {
                System.out.println("Creating new JSON file: " + file.getAbsolutePath());
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

    // public static void main(String[] args) throws Exception {
    // new TaskLoader().loadTasks();
    // }

    // public static void addTask(String taskPath) {
    // Thread.ofVirtual().start(() -> {
    // TaskLoader taskLoader = new TaskLoader();
    // taskLoader.loadTasks();
    // taskLoader.json.getJSONArray("tasks").put(taskPath);
    // taskLoader.saveJson();
    // });
    // }

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
            log.severe("Error occurred when saving JSON file: " + e.getMessage());
        }

    }

    public void addTasksFromJar(String jarPath, String overrideStartTime, Integer overrideInterval) {
        File jarFile = new File(jarPath);
        URLClassLoader classLoader = null;
        try {
            URL jarUrl = jarFile.toURI().toURL();

            // Create a URLClassLoader to load the JAR
            classLoader = new URLClassLoader(new URL[] { jarUrl }, ClassLoader.getSystemClassLoader());

            // Configure Reflections to scan the JAR
            Reflections reflections = new Reflections(new ConfigurationBuilder()
                    .setUrls(ClasspathHelper.forClassLoader(classLoader))
                    .setScanners(Scanners.TypesAnnotated));

            // Get all classes annotated with MyAnnotation.class
            Set<Class<?>> taskSubtypes = reflections.getTypesAnnotatedWith(com.mdr.task.annotations.Task.class);

            // Process the found classes
            for (Class<?> clazz : taskSubtypes) {
                Task taskAnnotation = clazz.getAnnotation(Task.class);
                TaskRecord task = Arrays.stream(clazz.getMethods())
                        .filter(method -> method.isAnnotationPresent(Executer.class)
                                && method.getParameterCount() == 0)
                        .map(method -> createTaskRecord(jarPath, clazz, taskAnnotation, method,
                                overrideStartTime, overrideInterval))
                        .findFirst()
                        .orElse(null);

                if (task != null) {
                    try {
                        loadJson();
                        json.getJSONArray("tasks").put(task.toJson());
                        saveJson();
                    } catch (IOException e) {
                        log.severe("Error occurred when saving task to JSON: " + e.getMessage());
                    }
                }
            }
        } catch (Exception e) {
            log.severe("Error occurred when loading tasks: " + e.getMessage());
        } finally {
            if (classLoader != null) {
                try {
                    classLoader.close();
                } catch (IOException e) {
                    log.severe("Error occurred when closing class loader: " + e.getMessage());
                }
            }
        }
    }

    // List<TaskRecord> loadTasksFromJson() {
    // List<TaskRecord> taskRecords = new ArrayList<>();
    // BufferedReader bufferedReader = null;
    // try {
    // File file = new File(Paths.get(TASK_FOLDER, JSON).toUri());
    // if (!file.exists()) {
    // file.createNewFile();
    // }
    // InputStreamReader reader = new InputStreamReader(
    // new FileInputStream(file));

    // bufferedReader = new BufferedReader(reader);
    // String jsonString = bufferedReader.lines().reduce("", (acc, line) -> {
    // if (line.trim().length() > 0) {
    // acc += line.trim();
    // }
    // return acc;
    // });
    // if (jsonString.isEmpty()) {
    // jsonString = "{\"tasks\":[]}";
    // json = new JSONObject(jsonString);
    // saveJson();
    // } else {
    // json = new JSONObject(jsonString);
    // }

    // JSONArray tasksArray = json.getJSONArray("tasks");
    // for (int i = 0; i < tasksArray.length(); i++) {
    // TaskRecord task = TaskRecord.fromJson(tasksArray.getJSONObject(i));
    // taskRecords.add(task);
    // }
    // } catch (Exception e) {
    // log.severe("Error occurred when loading tasks from JSON: " + e.getMessage());
    // } finally {
    // if (bufferedReader != null) {
    // try {
    // bufferedReader.close();
    // } catch (Exception e) {
    // log.severe("Error occurred when closing BufferedReader: " + e.getMessage());
    // }
    // }
    // }
    // return taskRecords;
    // }

    // private List<String> loadTaskPathsFromFolder() {
    // List<String> taskPaths = new ArrayList<>();
    // File folder = new File(TASK_FOLDER);
    // File[] files = folder.listFiles((_, name) -> name.endsWith(".jar"));
    // for (File file : files) {
    // taskPaths.add(file.getAbsolutePath());
    // }
    // return taskPaths;
    // }

    // public List<TaskRecord> loadTasks() {
    // List<TaskRecord> taskRecords = new ArrayList<>();
    // taskRecords.addAll(loadTasksFromJson());
    // // taskRecords.addAll(loadTaskPathsFromFolder());

    // List<TaskRecord> tasks = new ArrayList<>();
    // for (TaskRecord record : taskRecords) {

    // // 1. Obtain the JAR file path
    // File jarFile = new File(record.jarPath());
    // URLClassLoader classLoader = null;
    // try {
    // URL jarUrl = jarFile.toURI().toURL();

    // // Create a URLClassLoader to load the JAR
    // classLoader = new URLClassLoader(new URL[] { jarUrl },
    // ClassLoader.getSystemClassLoader());

    // // Configure Reflections to scan the JAR
    // Reflections reflections = new Reflections(new ConfigurationBuilder()
    // .setUrls(ClasspathHelper.forClassLoader(classLoader))
    // .setScanners(Scanners.TypesAnnotated));

    // // Get all classes annotated with MyAnnotation.class
    // Set<Class<?>> taskSubtypes =
    // reflections.getTypesAnnotatedWith(com.mdr.task.annotations.Task.class);

    // // Process the found classes
    // for (Class<?> clazz : taskSubtypes) {
    // Task taskAnnotation = clazz.getAnnotation(Task.class);
    // TaskRecord task = Arrays.stream(clazz.getMethods())
    // .filter(method -> method.isAnnotationPresent(Executer.class)
    // && method.getParameterCount() == 0)
    // .map(method -> createTaskRecord(record.jarPath(), clazz, taskAnnotation,
    // method))
    // .findFirst()
    // .orElse(null);

    // if (task != null) {
    // tasks.add(task);
    // }
    // }
    // } catch (Exception e) {
    // log.severe("Error occurred when loading tasks: " + e.getMessage());
    // } finally {
    // if (classLoader != null) {
    // try {
    // classLoader.close();
    // } catch (IOException e) {
    // log.severe("Error occurred when closing class loader: " + e.getMessage());
    // }
    // }
    // }
    // }
    // return tasks;
    // }

    private TaskRecord createTaskRecord(String jarPath, Class<?> clazz, Task taskAnnotation, Method method,
            String overrideStartTime, Integer overrideInterval) {
        synchronized (jarPath.intern()) {
            method.setAccessible(true);
            return new TaskRecord(
                    jarPath,
                    taskAnnotation.name(),
                    LocalDateTime.parse(overrideStartTime != null
                            ? overrideStartTime
                            : taskAnnotation.startTime(),
                            Props.DATE_FORMAT.get()),
                    overrideInterval != null ? overrideInterval : taskAnnotation.interval(),
                    (Runnable) () -> {
                        try {
                            method.invoke(clazz.getDeclaredConstructor().newInstance());
                        } catch (Exception e) {
                            log.severe("Error occurred when executing task: " + e.getMessage());
                        }
                    });
        }
    }
}

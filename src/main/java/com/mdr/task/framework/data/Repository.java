package com.mdr.task.framework.data;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.lang.reflect.Method;
import java.net.URL;
import java.net.URLClassLoader;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import org.json.JSONObject;
import org.reflections.Reflections;
import org.reflections.scanners.Scanners;
import org.reflections.util.ClasspathHelper;
import org.reflections.util.ConfigurationBuilder;

public class Repository extends Storable implements Map<String, Storable> {
    private File folder;
    private final Map<String, Storable> internalMap = new HashMap<>();

    public Repository(String name) throws IOException {
        this(name, null);

    }

    public Repository(String name, Repository parent) throws IOException {
        super(name);
        // setParent is called and folder is updated in put
        if (parent != null) {
            parent.put(name, this);
        } else {
            this.folder = new File(name);
        }

        if (!folder.exists()) {
            folder.mkdirs();
        }
        this.load();
    }

    /**
     * Get the folder where this repository is stored.
     * If the repository has a parent, the folder is relative to the parent's
     * folder.
     * If the repository has no parent, the folder is absolute.
     * 
     * @return The folder where this repository is stored.
     */
    public File getFolder() {
        if (parent != null) {
            return new File(parent.getFolder(), name);
        }
        return folder;
    }

    /**
     * Set the parent repository and update the folder accordingly.
     * 
     * @param parent The parent repository.
     */
    @Override
    protected void setParent(Repository parent) {
        this.parent = parent;
        this.folder = getFolder();
    }

    /**
     * Returns the number of key-value mappings in this map. If the map contains
     * more than {@link Integer#MAX_VALUE} elements, returns
     * {@link Integer#MAX_VALUE}.
     * 
     * @return the number of key-value mappings in this map
     */
    @Override
    public int size() {
        return internalMap.size();
    }

    /**
     * @return true if this map contains no key-value mappings
     */
    @Override
    public boolean isEmpty() {
        return internalMap.isEmpty();
    }

    /**
     * @param key
     * @return true if this map contains a mapping for the specified key
     */
    @Override
    public boolean containsKey(Object key) {
        return internalMap.containsKey(key);
    }

    /**
     * @param value
     * @return true if this map maps one or more keys to the specified value
     */
    @Override
    public boolean containsValue(Object value) {
        return internalMap.containsValue(value);
    }

    /**
     * @param key
     * @return the value to which the specified key is mapped, or null if this map
     *         contains no mapping for the key
     */
    @Override
    public Storable get(Object key) {
        return internalMap.get(key);
    }

    /**
     * Preferred method to add a storable to the repository
     * 
     * @param value
     * @return
     */
    public Storable put(Storable value) {
        return put(value.getName(), value);
    }

    /**
     * Add the storable to the repository and store it in the filesystem
     * It is preffered to use {@link #put(Storable)} instead
     * 
     * @param key
     * @param value
     */
    @Override
    public Storable put(String key, Storable value) {
        value.setParent(this);
        try {
            value.store();
        } catch (IOException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
            return null;
        }
        return internalMap.put(key, value);
    }

    /**
     * Remove the storable from the repository and delete it from the filesystem
     * 
     * @param key
     * @return the previous value associated with key, or null if there was no
     *         mapping for key.
     *         (A null return can also indicate that the map previously associated
     *         null with key.)
     */
    @Override
    public Storable remove(Object key) {
        Storable value = internalMap.remove(key);
        if (value != null) {
            try {
                value.delete();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        return value;
    }

    /**
     * Preffered method to add multiple storables to the repository
     * Add all storables to the repository and store them in the filesystem
     * It is preffered to use {@link #put(Storable)} instead
     * 
     * @param storables
     */
    public void putAll(Storable... storables) {
        for (Storable storable : storables) {
            put(storable);
        }
    }

    /**
     * Use {@link #putAll(Storable...)} instead
     * Add all storables to the repository and store them in the filesystem
     * It is preffered to use {@link #put(Storable)} instead
     * 
     * @param storables
     */
    @Override
    public void putAll(Map<? extends String, ? extends Storable> m) {
        for (Entry<? extends String, ? extends Storable> entry : m.entrySet()) {
            put(entry.getKey(), entry.getValue());
        }
    }

    @Override
    public void clear() {
        internalMap.clear();
    }

    @Override
    public Set<String> keySet() {
        return internalMap.keySet();
    }

    @Override
    public Collection<Storable> values() {
        return internalMap.values();
    }

    @Override
    public Set<Entry<String, Storable>> entrySet() {
        return internalMap.entrySet();
    }

    @Override
    void store() throws IOException {
        if (!getFolder().exists()) {
            getFolder().mkdirs();
        }
        for (Storable item : internalMap.values()) {
            item.store();
        }
    }

    @Override
    Repository load() throws IOException {
        if (!folder.exists()) {
            throw new IOException("Repository folder does not exist: " + folder.getAbsolutePath());
        }

        StorableFactory storableFactory = new StorableFactory();
        for (File file : folder.listFiles()) {
            Storable item = storableFactory.create(file, this);
            if (item != null) {
                internalMap.put(item.getName(), item);
            }
        }
        return this;
    }

    @Override
    public void delete() throws IOException {
        for (Storable item : internalMap.values()) {
            item.delete();
        }
        if (folder.exists() && folder.isDirectory()) {
            folder.delete();
        }
    }

    @Override
    Map<String, Storable> get() {
        return internalMap;
    }

    @Override
    boolean matchType(File file) {
        return file.isDirectory();
    }
}

class StorableFactory {
    private static final URL url;
    static {
        try {
            url = new File("").toURI().toURL();
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }
    final static Set<String> storableNames = new HashSet<>();

    /**
     * Create a StorableFactory that can create Storable instances from files.
     * Construction is expensive as it uses reflection to find all classes that
     * extend Storable.
     */
    public StorableFactory() {
        // get storables from reflection
        ClassLoader classLoader = new URLClassLoader(new URL[] { url },
                ClassLoader.getSystemClassLoader());
        Reflections reflections = new Reflections(new ConfigurationBuilder()
                // .setClassLoaders(new ClassLoader[] { classLoader })
                .setUrls(ClasspathHelper.forClassLoader(classLoader))
                .setScanners(Scanners.SubTypes));

        storableNames.addAll(reflections.getStore().get("SubTypes")
                .get("com.mdr.task.framework.data.Storable"));
    }

    /**
     * Create a Storable instance from a file.
     * Uses reflection to find the appropriate Storable subclass for the file.
     * See {@link Storable#matchType(File)} for how the appropriate subclass is
     * determined.
     * 
     * @param file   The file to create the Storable from.
     * @param parent The parent repository of the Storable.
     * @return The created Storable, or null if no appropriate subclass was found.
     */
    public Storable create(File file, Repository parent) {
        String name = file.getName();
        for (String storableName : storableNames) {
            try {
                Class<?> clazz = Class.forName(storableName);
                Method matchTypeMethod = clazz.getDeclaredMethod("matchType", File.class);
                if (Storable.class.isAssignableFrom(clazz)) {
                    if (clazz == Repository.class) {
                        // special case for Repository to pass parent
                        Storable storable = new Repository(name, parent);
                        return storable;
                    }

                    Storable storable = (Storable) clazz.getConstructor(String.class).newInstance(name);
                    if (!(boolean) matchTypeMethod.invoke(storable, file)) {
                        continue;
                    }
                    storable.setParent(parent);
                    storable.load();
                    return storable;
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
        return null;
    }
}

class TestRunner {
    public static void main(String[] args) throws IOException {
        Repository repo1 = new Repository("data");
        Repository repo = new Repository("data2", repo1);
        // try {
        // repo1.load();
        // } catch (IOException e1) {
        // // TODO Auto-generated catch block
        // e1.printStackTrace();
        // }
        // repo.put("example", new StorableFile("example.txt", "This is a test file."));
        repo.put(new StorableString("greeting.txt", "Hello, World!"));
        repo.put(new StorableJson("config.json", new JSONObject().put("key", "value")));
        System.out.println("Repository size: " + repo.size());
        try {
            // StorableFile storedFile = (StorableFile) repo.get("example");
            StorableString storedString = (StorableString) repo.get("greeting.txt");
            StorableJson storedJson = (StorableJson) repo.get("config.json");
            if (storedString != null) {
                // File file = storedFile.load();
                // System.out.println("Loaded file: " + file.getAbsolutePath());
                // try (java.util.Scanner scanner = new java.util.Scanner(file)) {
                // while (scanner.hasNextLine()) {
                // System.out.println(scanner.nextLine());
                // }
                // }
                String text = storedString.load().get();
                System.out.println("Loaded string: " + text);

                JSONObject json = storedJson.load().get();
                System.out.println("Loaded JSON: " + json.toString(4));
            }
            repo1.delete();
            // repo.remove(storedJson.getName());
            // repo1.remove("data2");
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    // private static class StorableFile extends Storable<File> {

    // String text;

    // public StorableFile(String name, String text) {
    // super(name);
    // this.text = text;
    // }

    // @Override
    // public File store() throws IOException {
    // File file = new File(parent.getFolder(), getName());
    // // file.mkdirs();
    // file.createNewFile();
    // try (FileWriter writer = new FileWriter(file)) {
    // writer.write(text);
    // }
    // return file;
    // }

    // @Override
    // public File load() throws IOException {
    // File file = new File(parent.getFolder(), getName());
    // if (!file.exists()) {
    // throw new IOException("File does not exist: " + file.getAbsolutePath());
    // }
    // return file;
    // }

    // @Override
    // public String toString() {
    // return "File{name='" + getName() + "'}";
    // }

    // @Override
    // boolean matchType(File file) {
    // // TODO Auto-generated method stub
    // throw new UnsupportedOperationException("Unimplemented method 'matchType'");
    // }
    // }

    private static class StorableString extends Storable {
        static {
            StorableFactory.storableNames.add(StorableString.class.getName());
        }

        String text;

        public StorableString(String name) {
            super(name);
        }

        public StorableString(String name, String text) {
            super(name);
            this.text = text;
        }

        @Override
        public void store() throws IOException {
            File file = new File(parent.getFolder(), getName());
            // file.mkdirs();
            file.createNewFile();
            try (FileWriter writer = new FileWriter(file)) {
                writer.write(text);
            }
        }

        @Override
        public StorableString load() throws IOException {
            File file = new File(parent.getFolder(), getName());
            if (!file.exists()) {
                throw new IOException("File does not exist: " + file.getAbsolutePath());
            }
            StringBuilder sb = new StringBuilder();
            try (java.util.Scanner scanner = new java.util.Scanner(file)) {
                while (scanner.hasNextLine()) {
                    sb.append(scanner.nextLine()).append("\n");
                }
            }
            text = sb.toString().trim();
            return this;
        }

        @Override
        public String get() {
            return text;
        }

        @Override
        public String toString() {
            return "String{name='" + getName() + "', text='" + text + "'}";
        }

        @Override
        boolean matchType(File file) {
            return file.getName().endsWith(".txt");
        }
    }

    private static class StorableJson extends Storable {
        static {
            StorableFactory.storableNames.add(StorableJson.class.getName());
        }

        JSONObject json;

        public StorableJson(String name) {
            super(name);
        }

        public StorableJson(String name, JSONObject json) {
            super(name);
            this.json = json;
        }

        @Override
        public void store() throws IOException {
            File file = new File(parent.getFolder(), getName());
            // file.mkdirs();
            file.createNewFile();
            try (FileWriter writer = new FileWriter(file)) {
                writer.write(json.toString(4));
            }
        }

        @Override
        public StorableJson load() throws IOException {
            File file = new File(parent.getFolder(), getName());
            if (!file.exists()) {
                throw new IOException("File does not exist: " + file.getAbsolutePath());
            }
            StringBuilder sb = new StringBuilder();
            try (java.util.Scanner scanner = new java.util.Scanner(file)) {
                while (scanner.hasNextLine()) {
                    sb.append(scanner.nextLine()).append("\n");
                }
            }
            json = new JSONObject(sb.toString().trim());
            return this;
        }

        @Override
        public JSONObject get() {
            return json;
        }

        @Override
        public String toString() {
            return "Json{name='" + getName() + "', json=" + json.toString() + "}";
        }

        @Override
        boolean matchType(File file) {
            return file.getName().endsWith(".json");
        }
    }
}
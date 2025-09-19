package com.mdr.task.framework.data;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.lang.reflect.Method;
import java.net.URL;
import java.net.URLClassLoader;
import java.util.Collection;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;

import org.json.JSONObject;
import org.reflections.Reflections;
import org.reflections.scanners.Scanners;
import org.reflections.util.ClasspathHelper;
import org.reflections.util.ConfigurationBuilder;

// public class Repository<T extends Storable> implements Map<String, T> {
//     Class<T> itemClass;
//     Repository<T> parent;
//     File folder;

//     Logger log = Logger.getLogger(this.getClass().getName());

//     @Override
//     public int size() {
//         return listStored().size();
//     }

//     @Override
//     public boolean isEmpty() {
//         return !folder.exists() || folder.listFiles().length == 0;
//     }

//     @Override
//     public boolean containsKey(Object key) {
//         return findById((String) key) != null;
//     }

//     @Override
//     public boolean containsValue(Object value) {
//         return listStored().contains(value);
//     }

//     @Override
//     public T get(Object key) {
//         return findById((String) key);
//     }

//     @Override
//     public T put(String key, T value) {
//         try {
//             save(value);
//         } catch (IOException e) {
//             log.log(Level.SEVERE, "Failed to save item with key: " + key, e);
//         }
//         return value;
//     }

//     @Override
//     public T remove(Object key) {
//         T item = findById((String) key);
//         if (item != null) {
//             item.file.delete();
//         }
//         return item;
//     }

//     @Override
//     public void putAll(Map<? extends String, ? extends T> m) {
//         for (Entry<? extends String, ? extends T> entry : m.entrySet()) {
//             put(entry.getKey(), entry.getValue());
//         }
//     }

//     @Override
//     public void clear() {
//         for (T item : listStored()) {
//             item.file.delete();
//         }
//     }

//     @Override
//     public Set<String> keySet() {
//         Set<String> keys = new HashSet<>();
//         for (T item : listStored()) {
//             keys.add(item.getId());
//         }
//         return keys;
//     }

//     @Override
//     public Collection<T> values() {
//         return listStored();
//     }

//     @Override
//     public Set<Entry<String, T>> entrySet() {
//         Set<Entry<String, T>> entries = new HashSet<>();
//         for (T item : listStored()) {
//             entries.add(new AbstractMap.SimpleEntry<>(item.getId(), item));
//         }
//         return entries;
//     }

//     public Repository(Repository<T> parent, String folder, Class<T> itemClass) {
//         this.parent = parent;
//         this.folder = new File(parent.folder, folder);
//         this.itemClass = itemClass;
//         if (!this.folder.exists()) {
//             this.folder.mkdirs();
//         }
//     }

//     public Repository(String folder, Class<T> itemClass) {
//         this.folder = new File(folder);
//         this.itemClass = itemClass;
//         if (!this.folder.exists()) {
//             this.folder.mkdirs();
//         }
//     }

//     public Repository<T> newChild(String folder) {
//         return new Repository<>(this, folder, itemClass);
//     }    

//     private File save(T item) throws IOException {
//         if (item == null) {
//             throw new IllegalArgumentException("Item cannot be null");
//         }
//         return item.store(folder);
//     }

//     public File getFolder() {
//         return folder;
//     }

//     public List<T> listStored() {
//         List<T> items = new ArrayList<>();
//         File[] files = folder.listFiles();
//         if (files != null) {
//             for (File file : files) {
//                 try {
//                     T item = itemClass.getDeclaredConstructor(File.class).newInstance(file);
//                     items.add(item);
//                 } catch (Exception e) {
//                     log.warning("Failed to load item from file: " + file.getAbsolutePath() + " - " + e.getMessage());
//                 }
//             }
//         }
//         return items;
//     }

//     public T findById(String id) {
//         List<T> items = listStored();
//         for (T item : items) {
//             if (item.getId().equals(id)) {
//                 return item;
//             }
//         }
//         return null;
//     }

//     public boolean contains(String id) {
//         return findById(id) != null;
//     }

// }

// public class Repository implements Map<String, File> {
//     private final Map<String, File> internalMap = new HashMap<>();
//     private final File folder;

//     public Repository(File folder) {
//         this.folder = folder;
//     }

//     public File getFolder() {
//         return folder;
//     }

//     @Override
//     public int size() {
//         return internalMap.size();
//     }

//     @Override
//     public boolean isEmpty() {
//         return internalMap.isEmpty();
//     }

//     @Override
//     public boolean containsKey(Object key) {
//         return internalMap.containsKey(key);
//     }

//     @Override
//     public boolean containsValue(Object value) {
//         return internalMap.containsValue(value);
//     }

//     @Override
//     public File get(Object key) {
//         return internalMap.get(key);
//     }

//     @Override
//     public File put(String key, File value) {
//         return internalMap.put(key, new File(folder, value.getName()));
//     }

//     @Override
//     public File remove(Object key) {
//         return internalMap.remove(key);
//     }

//     @Override
//     public void putAll(Map<? extends String, ? extends File> m) {
//         internalMap.putAll(m);
//     }

//     @Override
//     public void clear() {
//         internalMap.clear();
//     }

//     @Override
//     public Set<String> keySet() {
//         return internalMap.keySet();
//     }

//     @Override
//     public Collection<File> values() {
//         return internalMap.values();
//     }

//     @Override
//     public Set<Entry<String, File>> entrySet() {
//         return internalMap.entrySet();
//     }
// }

public class Repository extends Storable implements Map<String, Storable> {
    private File folder;
    private final Map<String, Storable> internalMap = new HashMap<>();

    public Repository(String name) throws IOException {
        super(name);
        this.folder = new File(name);
        if (!folder.exists()) {
            folder.mkdirs();
        }
        this.load();
    }

    public Repository(String name, Repository parent) throws IOException {
        this(name);
        //setParent is called and folder is updated in put
        parent.put(name, this);
    }

    public File getFolder() {
        if (parent != null) {
            return new File(parent.getFolder(), name);
        }
        return folder;
    }

    @Override
    public void setParent(Repository parent) {
        this.parent = parent;
        this.folder = getFolder();
    }

    @Override
    public int size() {
        return internalMap.size();
    }

    @Override
    public boolean isEmpty() {
        return internalMap.isEmpty();
    }

    @Override
    public boolean containsKey(Object key) {
        return internalMap.containsKey(key);
    }

    @Override
    public boolean containsValue(Object value) {
        return internalMap.containsValue(value);
    }

    @Override
    public Storable get(Object key) {
        return internalMap.get(key);
    }

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

    @Override
    public Storable remove(Object key) {
        return internalMap.remove(key);
    }

    @Override
    public void putAll(Map<? extends String, ? extends Storable> m) {
        internalMap.putAll(m);
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
        // if (parent != null) {
        //     // parent.load();
        // }
        for (File file : folder.listFiles()) {
            Storable item = StorableFactory.create(file, this);
            if (item != null) {
                internalMap.put(item.getName(), item);
            }
        }
        return this;
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
    public static Storable create(File file, Repository parent) {
        String name = file.getName();
        // get storables from reflection
        ClassLoader classLoader = new URLClassLoader(new URL[] { url },
                ClassLoader.getSystemClassLoader());
        Reflections reflections = new Reflections(new ConfigurationBuilder()
                // .setClassLoaders(new ClassLoader[] { classLoader })
                .setUrls(ClasspathHelper.forClassLoader(classLoader))
                .setScanners(Scanners.SubTypes));

        Set<String> storableNames = reflections.getStore().get("SubTypes")
                .get("com.mdr.task.framework.data.Storable");

        for (String storableName : storableNames) {
            try {
                Class<?> clazz = Class.forName(storableName);
                Method matchTypeMethod = clazz.getDeclaredMethod("matchType", File.class);
                // boolean matches = (boolean) matchTypeMethod.invoke(storable, file);
                if (Storable.class.isAssignableFrom(clazz)) {

                    Storable storable = (Storable) clazz.getConstructor(String.class).newInstance(name);
                    if (!(boolean) matchTypeMethod.invoke(storable, file)) {
                        continue;
                    }
                    storable.setParent(parent);
                    storable.load();
                    // storable.load();
                    // Storable<?> storable = (Storable<?>)
                    // clazz.getDeclaredConstructor(String.class).newInstance(name);
                    return storable;//parent.put(name, storable);
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
        return null;
    }
}

abstract class Storable {
    Repository parent;
    final String name;

    private Storable() {
        this.name = null;
    }

    public Storable(String name) {
        this.name = name;
    }

    public String getName() {
        return name;
    }

    void setParent(Repository parent) {
        this.parent = parent;
    }

    abstract void store() throws IOException;

    abstract Storable load() throws IOException;

    abstract Object get();

    abstract boolean matchType(File file);
}

class TestRunner {
    public static void main(String[] args) throws IOException {
        Repository repo1 = new Repository("data");
        Repository repo = (Repository) repo1.get("data2");
        // try {
        //     repo1.load();
        // } catch (IOException e1) {
        //     // TODO Auto-generated catch block
        //     e1.printStackTrace();
        // }
        // repo.put("example", new StorableFile("example.txt", "This is a test file."));
        // repo.put("greeting", new StorableString("greeting.txt", "Hello, World!"));
        // repo.put("config", new StorableJson("config.json", new
        // JSONObject().put("key", "value")));
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
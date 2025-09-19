package com.mdr.task.framework.data;

import java.io.File;
import java.util.Map;

// public abstract class Storable {

//     public final File file;

//     public Storable(File file) {
//         this.file = file;
//     }

//     public abstract File store(File parentFolder) throws IOException;
//     public abstract String getId();
//     public abstract File getFile();
// }

// public class Storable implements Map<String, File> {

//     @Override
//     public int size() {
//         return 0;
//     }

//     @Override
//     public boolean isEmpty() {
//         return false;
//     }

//     @Override
//     public boolean containsKey(Object key) {
//         return false;
//     }

//     @Override
//     public boolean containsValue(Object value) {
//         return false;
//     }

//     @Override
//     public File get(Object key) {
//         return null;
//     }

//     @Override
//     public File put(String key, File value) {
//         return null;
//     }

//     @Override
//     public File remove(Object key) {
//         return null;
//     }

//     @Override
//     public void putAll(Map<? extends String, ? extends File> m) {

//     }

//     @Override
//     public void clear() {

//     }

//     @Override
//     public java.util.Set<String> keySet() {
//         return null;
//     }

//     @Override
//     public java.util.Collection<File> values() {
//         return null;
//     }

//     @Override
//     public java.util.Set<Entry<String, File>> entrySet() {
//         return null;
//     }
// }
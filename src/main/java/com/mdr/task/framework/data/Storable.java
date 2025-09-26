package com.mdr.task.framework.data;

import java.io.File;
import java.io.IOException;

public abstract class Storable {
    Repository parent;
    final String name;

    /**
     * Create a storable with the given name.
     * The name is used as the filename in the repository folder.
     * 
     * @param name
     */
    public Storable(String name) {
        this.name = name;
    }

    public String getName() {
        return name;
    }

    void setParent(Repository parent) {
        this.parent = parent;
    }

    /**
     * Store the storable in the filesystem.
     * The storable is stored in the folder of the parent repository.
     * 
     * @throws IOException
     */
    public abstract void store() throws IOException;

    /**
     * Load the storable from the filesystem.
     * The storable is loaded from the folder of the parent repository.
     * 
     * @throws IOException
     */
    public abstract Storable load() throws IOException;

    /**
     * Delete the storable from the filesystem.
     * The storable is deleted from the folder of the parent repository.
     * 
     * @throws IOException
     */
    public void delete() throws IOException {
        File file = new File(parent.getFolder(), getName());
        if (file.exists()) {
            if (!file.delete()) {
                throw new IOException("Failed to delete file: " + file.getAbsolutePath());
            }
        }
    }

    /**
     * Get the folder where the storable is stored.
     * The folder is the folder of the parent repository.
     * 
     * @return the folder where the storable is stored.
     */
    public File getFolder() {
        return parent.getFolder();
    }

    /**
     * Get the value stored in the storable.
     * The type of the value depends on the implementation of the storable.
     * Intended to return the Object representation of the underlying file.
     * 
     * @return the value stored in the storable.
     */
    public abstract Object get();

    /**
     * Check if the file matches the type of the storable.
     * Used by the {@link StorableFactory} to determine which storable to create for
     * a given file.
     * {@link StorableFactory} uses reflection to call this method and decide what
     * type of storable to create.
     * 
     * @param file
     * @return true if the file matches the type of the storable.
     */
    public abstract boolean matchType(File file);
}
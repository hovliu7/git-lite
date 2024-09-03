package gitlet;

import java.io.Serializable;
import java.util.HashMap;

public class StagingArea implements Serializable {

    /** Files staged for addition. */
    private HashMap<String, String> added;

    /** Files that have been removed through -rm. */
    private HashMap<String, String> removed;

    /**
     * Creates new staging area.
     */
    public StagingArea() {
        added = new HashMap<String, String>();
        removed = new HashMap<String, String>();
    }

    /**
     * Adds new blob to be staged for addition.
     * @param fileName
     * @param hashcode
     */
    public void add(String fileName, String hashcode) {
        added.put(fileName, hashcode);

    }


    /**
     * Adds new blobs to be staged for removal.
     * @param fileName
     * @param hashcode
     */
    public void addToRemoved(String fileName, String hashcode) {
        removed.put(fileName, hashcode);
    }

    /**
     * Clears the staging area.
     */
    public void clear() {
        added = new HashMap<String, String>();
        removed = new HashMap<String, String>();
    }

    /**
     * Returns hashmap of files staged for addition.
     * @return
     */
    public HashMap<String, String> getAdded() {
        return this.added;
    }

    /**
     * Returns hashmap of files removed.
     * @return
     */
    public HashMap<String, String> getRemoved() {
        return this.removed;
    }

}

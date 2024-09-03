package gitlet;

import java.io.Serializable;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashMap;
import java.util.TimeZone;

public class Commit implements Serializable {

    /** Stores commit message. */
    private String _message;

    /** Stores commit timestamp. */
    private String _timestamp;

    /** Stores hash of current commit object. */
    private String _thisHash;

    /** Stores hash of parent's commit object. */
    private String _parentHash;

    /** Stores HashMap of blobs, <filename, SHA1 of file contents>. */
    private HashMap<String, String> _blobs;

    /** Stores hash of merge parent's commit object. */
    private String _mergeParentHash;

    /**
     * Creates a new Commit object that tracks the message, the parent's hash,
     * and the blobs/file contents.
     * @param message
     * @param parentHash
     * @param blobs
     * @param mParentHash
     */
    public Commit(String message, String parentHash,
                  HashMap<String, String> blobs, String mParentHash) {
        _message = message;
        _parentHash = parentHash;
        _blobs = blobs;
        Date currTime;
        TimeZone pstTimeZone = TimeZone.getTimeZone("America/Los_Angeles");
        if (_parentHash.equals("")) {
            currTime = new Date(0);
        } else {
            currTime = new Date();
        }
        SimpleDateFormat formatter =
                new SimpleDateFormat("EEE MMM d HH:mm:ss yyyy Z");
        formatter.setTimeZone(pstTimeZone);
        _timestamp = formatter.format(currTime);
        byte[] serializedThis = Utils.serialize(this);
        _thisHash = Utils.sha1(serializedThis);
        _mergeParentHash = mParentHash;
    }

    /**
     * Returns hashcode of this commit object.
     * @return
     */
    public String getHash() {
        return _thisHash;
    }

    /**
     * Returns message of this commit object.
     * @return
     */
    public String getMessage() {
        return _message;
    }

    /**
     * Returns timestamp of this commit object.
     * @return
     */
    public String getTimestamp() {
        return _timestamp;
    }

    /**
     * Returns hashcode of parent commit object.
     * @return
     */
    public String getParentHash() {
        return _parentHash;
    }

    /**
     * Returns hashmap of blobs.
     * @return
     */
    public HashMap<String, String> getBlobs() {
        return _blobs;
    }

    /**
     * Returns hashcode of merge parent commit object.
     * @return
     */
    public String getMergeParentHash() {
        return _mergeParentHash;
    }
}

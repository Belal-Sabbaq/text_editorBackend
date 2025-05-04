package com.project.backend.crdt;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

public class CRDTNode {
    private final String id;
    private final Character character;
    private final long timestamp;
    private final String siteId;
    private String leftId;
    private boolean deleted;
    private long deletedTimestamp;

    public CRDTNode(String id, Character character, long timestamp, String siteId, String leftId) {
        this.id = id;
        this.character = character;
        this.timestamp = timestamp;
        this.siteId = siteId;
        this.leftId = leftId;
        this.deleted = false;
        this.deletedTimestamp = 0L;
    }

    // Getters and Setters
    public String getId() {
        return id;
    }

    public Character getCharacter() {
        return character;
    }

    public long getTimestamp() {
        return timestamp;
    }

    public String getSiteId() {
        return siteId;
    }

    public String getLeftId() {
        return leftId;
    }

    public void setLeftId(String leftId) {
        this.leftId = leftId;
    }

    public boolean isDeleted() {
        return deleted;
    }

    public void setDeleted(boolean deleted) {
        this.deleted = deleted;
    }

    public long getDeletedTimestamp() {
        return deletedTimestamp;
    }

    public void setDeletedTimestamp(long deletedTimestamp) {
        this.deletedTimestamp = deletedTimestamp;
    }
}

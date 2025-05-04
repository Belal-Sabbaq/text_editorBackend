package com.project.backend.crdt;

enum OperationType {
    INSERT,
    DELETE
}

public class CRDTOperation {
    private final OperationType type;
    private final String nodeId;
    private final Character character;
    private final long timestamp;
    private final String siteId;
    private final String leftId;

    // Constructor for insert operations
    public CRDTOperation(String nodeId, Character character, long timestamp, String siteId, String leftId) {
        this.type = OperationType.INSERT;
        this.nodeId = nodeId;
        this.character = character;
        this.timestamp = timestamp;
        this.siteId = siteId;
        this.leftId = leftId;
    }

    // Constructor for delete operations
    public CRDTOperation(OperationType type, String nodeId, long timestamp) {
        this.type = type;
        this.nodeId = nodeId;
        this.character = null;
        this.timestamp = timestamp;
        this.siteId = null;
        this.leftId = null;
    }

    // Getters
    public OperationType getType() {
        return type;
    }

    public String getNodeId() {
        return nodeId;
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
}

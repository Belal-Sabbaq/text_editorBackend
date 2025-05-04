package com.project.backend.crdt;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;
public class TreeCRDT {
    private final String siteId;
    private final Map<String, CRDTNode> nodeMap;
    private final CRDTNode root;

    public TreeCRDT(String siteId) {
        this.siteId = siteId;
        this.nodeMap = new HashMap<>();
        this.root = new CRDTNode("root", null, System.currentTimeMillis(), siteId, null);
        this.nodeMap.put(root.getId(), root);
    }

    /**
     * Insert a character at a specified position
     * @param char character to insert
     * @param position position to insert at
     * @return the created node
     */
    public CRDTNode insert(char character, int position) {
        List<CRDTNode> visibleNodes = getVisibleNodes();

        CRDTNode leftNode = position == 0 ? root : visibleNodes.get(position - 1);
        CRDTNode rightNode = position >= visibleNodes.size() ? null : visibleNodes.get(position);

        // Create identifier for the new node
        String id = UUID.randomUUID().toString();
        long timestamp = System.currentTimeMillis();

        CRDTNode newNode = new CRDTNode(id, character, timestamp, siteId, leftNode.getId());
        nodeMap.put(id, newNode);

        // Update node relationships
        if (rightNode != null) {
            rightNode.setLeftId(newNode.getId());
        }

        return newNode;
    }

    /**
     * Delete a character at a specified position
     * @param position position to delete
     * @return the deleted node
     */
    public CRDTNode delete(int position) {
        List<CRDTNode> visibleNodes = getVisibleNodes();

        if (position < 0 || position >= visibleNodes.size()) {
            throw new IndexOutOfBoundsException("Invalid position for deletion");
        }

        CRDTNode nodeToDelete = visibleNodes.get(position);
        nodeToDelete.setDeleted(true);
        nodeToDelete.setDeletedTimestamp(System.currentTimeMillis());

        return nodeToDelete;
    }

    /**
     * Apply a remote operation (insert or delete)
     * @param operation the operation to apply
     */
    public void applyRemoteOperation(CRDTOperation operation) {
        if (operation.getType() == OperationType.INSERT) {
            applyRemoteInsert(operation);
        } else if (operation.getType() == OperationType.DELETE) {
            applyRemoteDelete(operation);
        }
    }

    private void applyRemoteInsert(CRDTOperation operation) {
        CRDTNode newNode = new CRDTNode(
                operation.getNodeId(),
                operation.getCharacter(),
                operation.getTimestamp(),
                operation.getSiteId(),
                operation.getLeftId()
        );

        nodeMap.put(newNode.getId(), newNode);

        // Update the right node's leftId if it exists
        for (CRDTNode node : nodeMap.values()) {
            if (node.getLeftId() != null && node.getLeftId().equals(operation.getLeftId())
                    && !node.getId().equals(newNode.getId())) {

                // Last-Writer-Wins conflict resolution
                if (node.getTimestamp() < newNode.getTimestamp() ||
                        (node.getTimestamp() == newNode.getTimestamp() &&
                                node.getSiteId().compareTo(newNode.getSiteId()) < 0)) {
                    node.setLeftId(newNode.getId());
                } else {
                    newNode.setLeftId(node.getId());
                }
                break;
            }
        }
    }

    private void applyRemoteDelete(CRDTOperation operation) {
        CRDTNode nodeToDelete = nodeMap.get(operation.getNodeId());
        if (nodeToDelete != null) {
            // LWW for deletion - apply whichever has the later timestamp
            if (!nodeToDelete.isDeleted() || nodeToDelete.getDeletedTimestamp() < operation.getTimestamp()) {
                nodeToDelete.setDeleted(true);
                nodeToDelete.setDeletedTimestamp(operation.getTimestamp());
            }
        }
    }

    /**
     * Get all visible nodes in order
     * @return list of visible nodes
     */
    public List<CRDTNode> getVisibleNodes() {
        List<CRDTNode> result = new ArrayList<>();
        CRDTNode current = findRightNeighbor(root.getId());

        while (current != null) {
            if (!current.isDeleted()) {
                result.add(current);
            }
            current = findRightNeighbor(current.getId());
        }

        return result;
    }

    /**
     * Convert the CRDT structure to a string
     * @return the document content as a string
     */
    public String toString() {
        StringBuilder sb = new StringBuilder();
        for (CRDTNode node : getVisibleNodes()) {
            sb.append(node.getCharacter());
        }
        return sb.toString();
    }

    private CRDTNode findRightNeighbor(String nodeId) {
        for (CRDTNode node : nodeMap.values()) {
            if (node.getLeftId() != null && node.getLeftId().equals(nodeId)) {
                return node;
            }
        }
        return null;
    }

}

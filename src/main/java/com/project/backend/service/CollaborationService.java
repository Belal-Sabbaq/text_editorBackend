package com.project.backend.service;
import com.project.backend.controller.CRDTOperationMessage;
import com.project.backend.crdt.CRDTNode;
import com.project.backend.crdt.CRDTOperation;
import com.project.backend.crdt.OperationType;
import com.project.backend.crdt.TreeCRDT;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;



@Service
public class CollaborationService {

    @Autowired
    private SessionService sessionService;

    public CRDTOperationMessage processOperation(Long sessionId, CRDTOperationMessage message) {
        TreeCRDT crdt = sessionService.getCRDTForSession(sessionId);

        if (crdt == null) {
            return null;
        }

        // Process operation based on type
        if (message.getType() == OperationType.INSERT) {
            processInsertOperation(crdt, message);
        } else if (message.getType() == OperationType.DELETE) {
            processDeleteOperation(crdt, message);
        }

        // Update session content in database
        sessionService.updateSessionContent(sessionId);

        return message;
    }

    private void processInsertOperation(TreeCRDT crdt, CRDTOperationMessage message) {
        if (message.getNodeId() == null || message.getTimestamp() == null) {
            // This is a local operation, not yet processed by CRDT
            if (message.getCharacter() != null && message.getPosition() != null) {
                CRDTNode node = crdt.insert(message.getCharacter(), message.getPosition());

                // Update message with generated node data for broadcasting
                message.setNodeId(node.getId());
                message.setTimestamp(node.getTimestamp());
                message.setSiteId(node.getSiteId());
                message.setLeftId(node.getLeftId());
            }
        } else {
            // This is a remote operation with complete data, apply directly
            CRDTOperation operation = new CRDTOperation(
                    message.getNodeId(),
                    message.getCharacter(),
                    message.getTimestamp(),
                    message.getSiteId(),
                    message.getLeftId()
            );
            crdt.applyRemoteOperation(operation);
        }
    }

    private void processDeleteOperation(TreeCRDT crdt, CRDTOperationMessage message) {
        if (message.getNodeId() == null) {
            // This is a local delete by position
            if (message.getPosition() != null) {
                CRDTNode node = crdt.delete(message.getPosition());

                // Update message with node data for broadcasting
                message.setNodeId(node.getId());
                message.setTimestamp(System.currentTimeMillis());
            }
        } else {
            // This is a remote delete with node ID
            CRDTOperation operation = new CRDTOperation(
                    OperationType.DELETE,
                    message.getNodeId(),
                    message.getTimestamp()
            );
            crdt.applyRemoteOperation(operation);
        }
    }
}
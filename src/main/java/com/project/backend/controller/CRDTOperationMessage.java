package com.project.backend.controller;

import com.project.backend.crdt.OperationType;
import lombok.Getter;
import lombok.Setter;

@Setter
@Getter
public class CRDTOperationMessage {
    private String sessionId;
    private String participantId;
    private OperationType type;
    private String nodeId;
    private Character character;
    private Long timestamp;
    private String siteId;
    private String leftId;
    private Integer position;
}

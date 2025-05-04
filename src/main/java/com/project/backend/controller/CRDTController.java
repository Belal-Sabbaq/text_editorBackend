package com.project.backend.controller;

import com.project.backend.service.CollaborationService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.messaging.handler.annotation.DestinationVariable;
import org.springframework.messaging.handler.annotation.MessageMapping;
import org.springframework.messaging.handler.annotation.SendTo;
import org.springframework.stereotype.Service;

@Service
public class CRDTController {

        @Autowired
        private CollaborationService collaborationService;

        @MessageMapping("/session/{sessionId}/operation")
        @SendTo("/topic/session/{sessionId}")
        public CRDTOperationMessage handleOperation(@DestinationVariable String sessionId,
                                                    CRDTOperationMessage message) {
            return collaborationService.processOperation(Long.valueOf(sessionId), message);
        }
}

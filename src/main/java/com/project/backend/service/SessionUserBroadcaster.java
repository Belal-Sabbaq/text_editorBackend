package com.project.backend.service;

import com.project.backend.utils.SessionUserDTO;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class SessionUserBroadcaster {
@Autowired private SimpMessagingTemplate messagingTemplate;
public void broadCastUsers(Long sessionId, List<SessionUserDTO> users) {
    messagingTemplate.convertAndSend("/topic/session/" + sessionId + "/users",users);
}
}

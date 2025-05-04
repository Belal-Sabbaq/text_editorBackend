package com.project.backend.controller;

import com.project.backend.entity.Session;
import com.project.backend.entity.SessionUser;
import com.project.backend.entity.User;
import com.project.backend.service.SessionService;
import com.project.backend.utils.SessionUserDTO;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/sessions")
public class SessionController {
    @Autowired
    private SessionService sessionService;

    @PostMapping("/create")
    public ResponseEntity<Session> createSessionWithOwner(@RequestBody Map<String, String> request) {
        try {
        Session session = sessionService.createSessionWithOwner(request.get("username"));
        return ResponseEntity.status(HttpStatus.CREATED).body(session);
        }
        catch (Exception e){
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }

    @PostMapping("/login")
    public ResponseEntity<String> loginToSession(@RequestBody Map<String, String> request) {
        return sessionService.loginToSession(request.get("username"),request.get("accessCode"));
    }

    @GetMapping("/{sessionId}/codes")
    public Map<String, String> getSessionCodes(@PathVariable long sessionId, @RequestParam String username) {
        return sessionService.getSessionCodes(sessionId, username);
    }
}

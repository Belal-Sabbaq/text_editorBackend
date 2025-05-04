package com.project.backend.service;

import com.project.backend.entity.Session;
import com.project.backend.entity.SessionUser;
import com.project.backend.entity.User;
import com.project.backend.repository.SessionRepository;
import com.project.backend.repository.SessionUserRepository;
import com.project.backend.repository.UserRepository;
import com.project.backend.utils.SessionUserDTO;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

import java.util.*;
import java.util.stream.Collectors;

@Service
public class SessionService {
    @Autowired private SessionRepository sessionRepo;
    @Autowired private UserRepository userRepo;
    @Autowired private SessionUserRepository sessionUserRepo;
    @Autowired private SessionUserBroadcaster broadcaster;

    public Session createSessionWithOwner(String username) {
        String viewCode, editCode;
        boolean codesExist;

        do {
            viewCode = generateRandomCode(8);
            editCode = generateRandomCode(8);
            codesExist = sessionRepo.existsBySessionCodeViewOrSessionCodeEdit(viewCode, editCode);
        } while (codesExist);

        User user = userRepo.findByUsername(username).orElseGet(() -> {
            User u = new User();
            u.setUsername(username);
            return userRepo.save(u);
        });

        Session session = new Session();
        session.setSessionCodeView(viewCode);
        session.setSessionCodeEdit(editCode);
        session = sessionRepo.save(session);

        SessionUser su = new SessionUser();
        su.setSession(session);
        su.setUser(user);
        su.setRole(SessionUser.Role.OWNER);
        su.setActive(true);
        sessionUserRepo.save(su);

        broadcastSessionUsers(session.getId());
        return session;
    }

    public ResponseEntity<String> loginToSession(String username, String accessCode) {        Session session = sessionRepo.findAll().stream()
                .filter(s -> accessCode.equals(s.getSessionCodeEdit()) || accessCode.equals(s.getSessionCodeView()))
                .findFirst()
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Invalid code"));

        SessionUser.Role role = accessCode.equals(session.getSessionCodeEdit()) ? SessionUser.Role.EDITOR : SessionUser.Role.VIEWER;

        User user = userRepo.findByUsername(username).orElseGet(() -> {
            User u = new User();
            u.setUsername(username);
            return userRepo.save(u);
        });

        SessionUser su = sessionUserRepo.findBySessionIdAndUserId(session.getId(), user.getId()).orElse(new SessionUser());
        su.setSession(session);
        su.setUser(user);
        su.setRole(role);
        su.setActive(true);
        sessionUserRepo.save(su);

        broadcastSessionUsers(session.getId());
        return ResponseEntity.ok("Logged in with role: "+ role);
    }

    public Map<String, String> getSessionCodes(Long sessionId, String username) {
        User user = userRepo.findByUsername(username)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "User Not Found"));

        SessionUser sessionUser = sessionUserRepo.findBySessionIdAndUserId(sessionId, user.getId())
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.FORBIDDEN, "User has no access"));

        Session session = sessionRepo.findById(sessionId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Session Not Found"));

        Map<String, String> response = new HashMap<>();
        if (sessionUser.getRole() == SessionUser.Role.VIEWER) {
            response.put("viewCode", "-1");
            response.put("editCode", "-1");
        } else {
            response.put("viewCode", session.getSessionCodeView());
            response.put("editCode", session.getSessionCodeEdit());
        }
        return response;
    }

    private void broadcastSessionUsers(Long sessionId) {
        List<SessionUser> sessionUsers = sessionUserRepo.findBySessionId(sessionId);
        List<SessionUserDTO> dtos = sessionUsers.stream()
                .map(su -> new SessionUserDTO(su.getUser().getUsername(), su.getRole().toString(), su.isActive()))
                .collect(Collectors.toList());
        broadcaster.broadCastUsers(sessionId, dtos);
    }

    private String generateRandomCode(int length) {
        String characters = "ABCDEFGHIJKLMNOPQRSTUVWXYZabcdefghijklmnopqrstuvwxyz0123456789";
        StringBuilder sb = new StringBuilder();
        Random random = new Random();
        for (int i = 0; i < length; i++) {
            sb.append(characters.charAt(random.nextInt(characters.length())));
        }
        return sb.toString();
    }
}

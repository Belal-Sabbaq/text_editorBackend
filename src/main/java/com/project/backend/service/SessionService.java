package com.project.backend.service;

import com.project.backend.entity.Session;
import com.project.backend.entity.SessionUser;
import com.project.backend.entity.User;
import com.project.backend.repository.SessionRepository;
import com.project.backend.repository.SessionUserRepository;
import com.project.backend.repository.UserRepository;
import com.project.backend.utils.SessionUserDTO;
import com.project.backend.crdt.TreeCRDT;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

import java.util.*;
import java.util.stream.Collectors;
import java.util.concurrent.ConcurrentHashMap;

@Service
public class SessionService {
    @Autowired private SessionRepository sessionRepo;
    @Autowired private UserRepository userRepo;
    @Autowired private SessionUserRepository sessionUserRepo;
    @Autowired private SessionUserBroadcaster broadcaster;

    private final ObjectMapper objectMapper = new ObjectMapper();
    private final Map<Long, TreeCRDT> activeCRDTs = new ConcurrentHashMap<>();

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

        // Initialize CRDT for this session
        TreeCRDT crdt = new TreeCRDT(session.getId().toString());
        activeCRDTs.put(session.getId(), crdt);
        updateSessionCRDTState(session);

        SessionUser su = new SessionUser();
        su.setSession(session);
        su.setUser(user);
        su.setRole(SessionUser.Role.OWNER);
        su.setActive(true);
        sessionUserRepo.save(su);

        broadcastSessionUsers(session.getId());
        return session;
    }

    public ResponseEntity<String> loginToSession(String username, String accessCode) {
        Session session = sessionRepo.findAll().stream()
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

        // Ensure CRDT is loaded for this session
        getCRDTForSession(session.getId());

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

    public TreeCRDT getCRDTForSession(Long sessionId) {
        // Return from cache if available
        if (activeCRDTs.containsKey(sessionId)) {
            return activeCRDTs.get(sessionId);
        }

        // Load from database if not in cache
        Session session = sessionRepo.findById(sessionId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Session Not Found"));

        try {
            if (session.getCrdtState() != null) {
                TreeCRDT crdt = objectMapper.readValue(session.getCrdtState(), TreeCRDT.class);
                activeCRDTs.put(sessionId, crdt);
                return crdt;
            }
        } catch (Exception e) {
            throw new ResponseStatusException(HttpStatus.INTERNAL_SERVER_ERROR, "Error loading CRDT state");
        }

        // Create new CRDT if none exists
        TreeCRDT crdt = new TreeCRDT(sessionId.toString());
        activeCRDTs.put(sessionId, crdt);
        updateSessionCRDTState(session);
        return crdt;
    }

    public void updateSessionContent(Long sessionId) {
        TreeCRDT crdt = getCRDTForSession(sessionId);
        if (crdt != null) {
            Session session = sessionRepo.findById(sessionId)
                    .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Session Not Found"));

            // Update content
            String content = crdt.toString();
            session.setContent(content);

            // Update serialized CRDT state
            updateSessionCRDTState(session);

            sessionRepo.save(session);
        }
    }

    private void updateSessionCRDTState(Session session) {
        TreeCRDT crdt = activeCRDTs.get(session.getId());
        if (crdt != null) {
            try {
                session.setCrdtState(objectMapper.writeValueAsString(crdt));
            } catch (Exception e) {
                throw new ResponseStatusException(HttpStatus.INTERNAL_SERVER_ERROR, "Error serializing CRDT state");
            }
        }
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
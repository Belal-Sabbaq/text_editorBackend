package com.project.backend.repository;

import com.project.backend.entity.SessionUser;
import com.project.backend.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface SessionUserRepository extends JpaRepository<SessionUser, Long> {
    List<SessionUser> findBySessionId(Long sessionId);
    Optional<SessionUser> findBySessionIdAndUserId(Long sessionId, Long userId);
}

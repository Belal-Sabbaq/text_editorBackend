package com.project.backend.repository;

import com.project.backend.entity.Session;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface SessionRepository extends JpaRepository<Session, Long> {
    boolean existsBySessionCodeViewOrSessionCodeEdit(String viewCode, String editCode);
}
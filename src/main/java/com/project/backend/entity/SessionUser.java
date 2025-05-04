package com.project.backend.entity;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

@Setter
@Getter
@Entity
@Table(name = "\"session_user\"")
public class SessionUser {
    @Id
    @GeneratedValue
    private Long id;

    @Column(name = "session_id", nullable = false)
    private Long sessionId;

    @jakarta.persistence.ManyToOne
    @jakarta.persistence.JoinColumn(name = "session_id", insertable = false, updatable = false)
    private Session session;

    @ManyToOne
    private User user;


    @Column(columnDefinition = "varchar(255)")
    private Role role;

    private boolean isActive;

    public enum Role {
        OWNER, EDITOR, VIEWER
    }
}
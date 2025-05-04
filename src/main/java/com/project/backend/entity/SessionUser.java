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

    @ManyToOne
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
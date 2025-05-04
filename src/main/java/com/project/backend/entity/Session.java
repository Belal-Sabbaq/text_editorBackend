package com.project.backend.entity;


import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

@Entity
@Setter
@Getter
@Table(name = "app_session")
public class Session {
    @Id
    @GeneratedValue
    private Long id;
    private String sessionCodeView;
    private String sessionCodeEdit;


    @Column(columnDefinition = "TEXT")
    private String content;

    @OneToMany(mappedBy = "session", cascade = CascadeType.ALL)
    private Set<SessionUser> sessionUsers = new HashSet<>();
    // Serialized CRDT state for persistence
    @Column(name = "crdt_state", columnDefinition = "TEXT")
    private String crdtState;


}

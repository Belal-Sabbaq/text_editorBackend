package com.project.backend.utils;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;

@Setter
@Getter
@AllArgsConstructor
public class SessionDTO {
    private String id;
    private String content;
    private long createdAt;
    private long updatedAt;
    private int participantCount;
}

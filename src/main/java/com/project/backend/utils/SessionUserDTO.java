package com.project.backend.utils;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@AllArgsConstructor
public class SessionUserDTO {
    private String username;
    private String role;
    private boolean isActive;


}

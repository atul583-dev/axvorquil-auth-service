package com.axvorquil.auth.dto;

import lombok.Builder;
import lombok.Data;

import java.time.LocalDateTime;

@Data
@Builder
public class UserDto {
    private String id;
    private String firstName;
    private String lastName;
    private String email;
    private String clinicRole;
    private boolean enabled;
    private LocalDateTime createdAt;
}

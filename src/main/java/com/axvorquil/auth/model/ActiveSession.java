package com.axvorquil.auth.model;

import lombok.*;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;
import java.time.LocalDateTime;

@Data @Builder @NoArgsConstructor @AllArgsConstructor
@Document(collection = "active_sessions")
public class ActiveSession {
    @Id
    private String id;          // = jti (JWT ID)
    private String userId;
    private String userEmail;
    private String firstName;
    private String lastName;
    private String ipAddress;
    private String userAgent;
    private String device;
    private String browser;
    @CreatedDate
    private LocalDateTime loginAt;
    @Builder.Default
    private boolean active = true;
}

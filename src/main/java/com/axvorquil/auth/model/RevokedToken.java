package com.axvorquil.auth.model;

import lombok.*;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.index.Indexed;
import org.springframework.data.mongodb.core.mapping.Document;
import java.util.Date;

@Data @Builder @NoArgsConstructor @AllArgsConstructor
@Document(collection = "revoked_tokens")
public class RevokedToken {
    @Id
    private String jti;
    @Indexed(expireAfterSeconds = 0)
    private Date expireAt;
}

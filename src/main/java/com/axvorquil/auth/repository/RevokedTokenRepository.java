package com.axvorquil.auth.repository;

import com.axvorquil.auth.model.RevokedToken;
import org.springframework.data.mongodb.repository.MongoRepository;

public interface RevokedTokenRepository extends MongoRepository<RevokedToken, String> {
    boolean existsByJti(String jti);
}

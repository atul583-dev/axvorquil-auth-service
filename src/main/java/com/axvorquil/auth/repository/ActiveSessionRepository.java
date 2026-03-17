package com.axvorquil.auth.repository;

import com.axvorquil.auth.model.ActiveSession;
import org.springframework.data.mongodb.repository.MongoRepository;
import java.util.List;

public interface ActiveSessionRepository extends MongoRepository<ActiveSession, String> {
    List<ActiveSession> findByActiveTrue();
    List<ActiveSession> findByUserIdAndActiveTrue(String userId);
    void deleteByUserId(String userId);
}

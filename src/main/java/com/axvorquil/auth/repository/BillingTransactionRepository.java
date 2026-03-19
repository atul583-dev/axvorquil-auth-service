package com.axvorquil.auth.repository;

import com.axvorquil.auth.model.BillingTransaction;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface BillingTransactionRepository extends MongoRepository<BillingTransaction, String> {
    List<BillingTransaction> findByOrgIdOrderByCreatedAtDesc(String orgId);
    Optional<BillingTransaction> findByRazorpayOrderId(String orderId);
}

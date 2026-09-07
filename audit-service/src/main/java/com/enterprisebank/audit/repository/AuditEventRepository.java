package com.enterprisebank.audit.repository;

import com.enterprisebank.audit.entity.AuditEvent;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface AuditEventRepository
        extends JpaRepository<AuditEvent, Long> {

    boolean existsByEventId(String eventId);

    Optional<AuditEvent> findByEventId(String eventId);

    List<AuditEvent> findByTransactionReferenceOrderByAuditedAtDesc(
            String transactionReference
    );
}
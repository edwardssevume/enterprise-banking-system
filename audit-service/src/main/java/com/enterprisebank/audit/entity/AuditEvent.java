package com.enterprisebank.audit.entity;

import jakarta.persistence.*;
import lombok.*;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Entity
@Table(
        name = "audit_events",
        uniqueConstraints = {
                @UniqueConstraint(
                        name = "uk_audit_event_id",
                        columnNames = "event_id"
                )
        }
)
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class AuditEvent {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "event_id", nullable = false, unique = true, length = 100)
    private String eventId;

    @Column(
            name = "transaction_reference",
            nullable = false,
            length = 50
    )
    private String transactionReference;

    @Column(
            name = "transaction_type",
            nullable = false,
            length = 30
    )
    private String transactionType;

    @Column(name = "source_account_id")
    private Long sourceAccountId;

    @Column(name = "destination_account_id")
    private Long destinationAccountId;

    @Column(
            nullable = false,
            precision = 19,
            scale = 2
    )
    private BigDecimal amount;

    @Column(nullable = false, length = 10)
    private String currency;

    @Column(length = 500)
    private String description;

    @Column(name = "initiated_by_user_id")
    private Long initiatedByUserId;

    @Column(name = "transaction_completed_at")
    private LocalDateTime transactionCompletedAt;

    @Column(
            name = "audited_at",
            nullable = false,
            updatable = false
    )
    private LocalDateTime auditedAt;

    @PrePersist
    public void prePersist() {
        if (auditedAt == null) {
            auditedAt = LocalDateTime.now();
        }
    }
}
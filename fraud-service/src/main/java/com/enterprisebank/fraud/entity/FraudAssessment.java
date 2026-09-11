package com.enterprisebank.fraud.entity;

import jakarta.persistence.*;
import lombok.*;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Entity
@Table(
        name = "fraud_assessments",
        uniqueConstraints = {
                @UniqueConstraint(
                        name = "uk_fraud_event_id",
                        columnNames = "event_id"
                )
        }
)
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class FraudAssessment {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(
            name = "event_id",
            nullable = false,
            unique = true,
            length = 100
    )
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

    @Column(name = "initiated_by_user_id")
    private Long initiatedByUserId;

    @Enumerated(EnumType.STRING)
    @Column(
            name = "risk_level",
            nullable = false,
            length = 20
    )
    private RiskLevel riskLevel;

    @Column(
            name = "risk_score",
            nullable = false
    )
    private Integer riskScore;

    @Column(
            name = "fraud_reason",
            nullable = false,
            length = 500
    )
    private String fraudReason;

    @Enumerated(EnumType.STRING)
    @Column(
            name = "review_status",
            nullable = false,
            length = 30
    )
    private ReviewStatus reviewStatus;

    @Column(name = "transaction_completed_at")
    private LocalDateTime transactionCompletedAt;

    @Column(
            name = "assessed_at",
            nullable = false,
            updatable = false
    )
    private LocalDateTime assessedAt;

    @Column(name = "reviewed_by_user_id")
    private Long reviewedByUserId;

    @Column(name = "reviewed_at")
    private LocalDateTime reviewedAt;

    @Column(name = "review_notes", length = 1000)
    private String reviewNotes;

    @PrePersist
    public void prePersist() {

        if (assessedAt == null) {
            assessedAt = LocalDateTime.now();
        }
    }
}
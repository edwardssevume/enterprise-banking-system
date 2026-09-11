package com.enterprisebank.fraud.repository;

import com.enterprisebank.fraud.entity.FraudAssessment;
import com.enterprisebank.fraud.entity.ReviewStatus;
import com.enterprisebank.fraud.entity.RiskLevel;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface FraudAssessmentRepository
        extends JpaRepository<FraudAssessment, Long> {

    boolean existsByEventId(String eventId);

    Optional<FraudAssessment> findByEventId(
            String eventId
    );

    List<FraudAssessment>
    findByTransactionReferenceOrderByAssessedAtDesc(
            String transactionReference
    );

    List<FraudAssessment>
    findByRiskLevelOrderByAssessedAtDesc(
            RiskLevel riskLevel
    );

    List<FraudAssessment>
    findByReviewStatusOrderByAssessedAtDesc(
            ReviewStatus reviewStatus
    );
}
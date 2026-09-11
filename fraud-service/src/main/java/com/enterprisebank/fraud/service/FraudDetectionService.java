package com.enterprisebank.fraud.service;

import com.enterprisebank.fraud.dto.FraudAssessmentResponse;
import com.enterprisebank.fraud.entity.FraudAssessment;
import com.enterprisebank.fraud.entity.ReviewStatus;
import com.enterprisebank.fraud.entity.RiskLevel;
import com.enterprisebank.fraud.event.TransactionCompletedEvent;
import com.enterprisebank.fraud.exception.FraudAssessmentNotFoundException;
import com.enterprisebank.fraud.exception.InvalidFraudReviewException;
import com.enterprisebank.fraud.repository.FraudAssessmentRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

@Service
@RequiredArgsConstructor
@Slf4j
public class FraudDetectionService {

    private static final BigDecimal MEDIUM_THRESHOLD =
            new BigDecimal("5000.00");

    private static final BigDecimal HIGH_THRESHOLD =
            new BigDecimal("10000.00");

    private final FraudAssessmentRepository fraudAssessmentRepository;

    // =========================================================
    // KAFKA FRAUD ASSESSMENT
    // =========================================================

    @Transactional
    public void assessTransaction(TransactionCompletedEvent event) {

        // Prevent the same Kafka event from being processed twice
        if (fraudAssessmentRepository.existsByEventId(event.eventId())) {

            log.info(
                    "Fraud event already assessed. eventId={}",
                    event.eventId()
            );

            return;
        }

        RiskLevel riskLevel;
        int riskScore;
        String reason;
        ReviewStatus reviewStatus;

        // HIGH RISK
        if (event.amount().compareTo(HIGH_THRESHOLD) >= 0) {

            riskLevel = RiskLevel.HIGH;
            riskScore = 90;

            reason =
                    "High-value transaction of "
                            + event.amount()
                            + " "
                            + event.currency();

            reviewStatus = ReviewStatus.PENDING_REVIEW;

        }

        // MEDIUM RISK
        else if (event.amount().compareTo(MEDIUM_THRESHOLD) >= 0) {

            riskLevel = RiskLevel.MEDIUM;
            riskScore = 50;

            reason =
                    "Medium-value transaction requires monitoring";

            reviewStatus = ReviewStatus.NOT_REQUIRED;

        }

        // LOW RISK
        else {

            riskLevel = RiskLevel.LOW;
            riskScore = 10;

            reason =
                    "Transaction within normal value threshold";

            reviewStatus = ReviewStatus.NOT_REQUIRED;
        }

        FraudAssessment assessment =
                FraudAssessment.builder()
                        .eventId(event.eventId())
                        .transactionReference(
                                event.transactionReference()
                        )
                        .transactionType(
                                event.transactionType()
                        )
                        .sourceAccountId(
                                event.sourceAccountId()
                        )
                        .destinationAccountId(
                                event.destinationAccountId()
                        )
                        .amount(event.amount())
                        .currency(event.currency())
                        .initiatedByUserId(
                                event.initiatedByUserId()
                        )
                        .riskLevel(riskLevel)
                        .riskScore(riskScore)
                        .fraudReason(reason)
                        .reviewStatus(reviewStatus)
                        .transactionCompletedAt(
                                event.completedAt()
                        )
                        .build();

        fraudAssessmentRepository.save(assessment);

        log.info(
                "Fraud assessment recorded. reference={}, riskLevel={}, riskScore={}",
                event.transactionReference(),
                riskLevel,
                riskScore
        );
    }

    // =========================================================
    // GET ALL FRAUD ASSESSMENTS
    // =========================================================

    @Transactional(readOnly = true)
    public List<FraudAssessmentResponse> getAllAssessments() {

        return fraudAssessmentRepository
                .findAll(
                        Sort.by(
                                Sort.Direction.DESC,
                                "assessedAt"
                        )
                )
                .stream()
                .map(this::mapToResponse)
                .toList();
    }

    // =========================================================
    // GET FRAUD ASSESSMENT BY ID
    // =========================================================

    @Transactional(readOnly = true)
    public FraudAssessmentResponse getAssessmentById(Long id) {

        FraudAssessment assessment =
                fraudAssessmentRepository
                        .findById(id)
                        .orElseThrow(
                                () -> new FraudAssessmentNotFoundException(
                                        "Fraud assessment not found: " + id
                                )
                        );

        return mapToResponse(assessment);
    }

    // =========================================================
    // GET ASSESSMENTS BY TRANSACTION REFERENCE
    // =========================================================

    @Transactional(readOnly = true)
    public List<FraudAssessmentResponse> getAssessmentsByTransaction(
            String transactionReference
    ) {

        return fraudAssessmentRepository
                .findByTransactionReferenceOrderByAssessedAtDesc(
                        transactionReference
                )
                .stream()
                .map(this::mapToResponse)
                .toList();
    }

    // =========================================================
    // GET HIGH-RISK ASSESSMENTS
    // =========================================================

    @Transactional(readOnly = true)
    public List<FraudAssessmentResponse> getHighRiskAssessments() {

        return fraudAssessmentRepository
                .findByRiskLevelOrderByAssessedAtDesc(
                        RiskLevel.HIGH
                )
                .stream()
                .map(this::mapToResponse)
                .toList();
    }

    // =========================================================
    // GET ASSESSMENTS PENDING REVIEW
    // =========================================================

    @Transactional(readOnly = true)
    public List<FraudAssessmentResponse> getPendingReviews() {

        return fraudAssessmentRepository
                .findByReviewStatusOrderByAssessedAtDesc(
                        ReviewStatus.PENDING_REVIEW
                )
                .stream()
                .map(this::mapToResponse)
                .toList();
    }

    @Transactional
    public FraudAssessmentResponse reviewAssessment(
            Long id,
            ReviewStatus newStatus,
            String reviewNotes,
            Long reviewerUserId
    ) {

        FraudAssessment assessment =
                fraudAssessmentRepository
                        .findById(id)
                        .orElseThrow(
                                () -> new FraudAssessmentNotFoundException(
                                        "Fraud assessment not found: " + id
                                )
                        );

        if (assessment.getReviewStatus()
                != ReviewStatus.PENDING_REVIEW) {

            throw new InvalidFraudReviewException(
                    "Only assessments with PENDING_REVIEW status can be reviewed"
            );
        }

        if (newStatus != ReviewStatus.CLEARED
                && newStatus != ReviewStatus.CONFIRMED_FRAUD) {

            throw new InvalidFraudReviewException(
                    "Review status must be CLEARED or CONFIRMED_FRAUD"
            );
        }

        assessment.setReviewStatus(newStatus);
        assessment.setReviewedByUserId(reviewerUserId);
        assessment.setReviewedAt(LocalDateTime.now());
        assessment.setReviewNotes(reviewNotes);

        FraudAssessment savedAssessment =
                fraudAssessmentRepository.save(assessment);

        log.info(
                "Fraud assessment reviewed. id={}, reference={}, status={}, reviewerUserId={}",
                savedAssessment.getId(),
                savedAssessment.getTransactionReference(),
                savedAssessment.getReviewStatus(),
                reviewerUserId
        );

        return mapToResponse(savedAssessment);
    }

    // =========================================================
    // ENTITY -> DTO MAPPER
    // =========================================================

    private FraudAssessmentResponse mapToResponse(
            FraudAssessment assessment
    ) {

        return new FraudAssessmentResponse(
                assessment.getId(),
                assessment.getEventId(),
                assessment.getTransactionReference(),
                assessment.getTransactionType(),
                assessment.getSourceAccountId(),
                assessment.getDestinationAccountId(),
                assessment.getAmount(),
                assessment.getCurrency(),
                assessment.getInitiatedByUserId(),
                assessment.getRiskLevel(),
                assessment.getRiskScore(),
                assessment.getFraudReason(),
                assessment.getReviewStatus(),
                assessment.getTransactionCompletedAt(),
                assessment.getAssessedAt(),

                assessment.getReviewedByUserId(),
                assessment.getReviewedAt(),
                assessment.getReviewNotes()
        );
    }
}
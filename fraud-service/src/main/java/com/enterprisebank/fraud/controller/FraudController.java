package com.enterprisebank.fraud.controller;

import com.enterprisebank.fraud.dto.FraudAssessmentResponse;
import com.enterprisebank.fraud.service.FraudDetectionService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import com.enterprisebank.fraud.dto.FraudReviewRequest;
import jakarta.validation.Valid;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.oauth2.jwt.Jwt;

import java.util.List;

@RestController
@RequestMapping("/api/fraud")
@RequiredArgsConstructor
public class FraudController {

    private final FraudDetectionService fraudDetectionService;

    @GetMapping("/assessments")
    public ResponseEntity<List<FraudAssessmentResponse>>
    getAllAssessments() {

        return ResponseEntity.ok(
                fraudDetectionService.getAllAssessments()
        );
    }

    @GetMapping("/assessments/{id}")
    public ResponseEntity<FraudAssessmentResponse>
    getAssessmentById(
            @PathVariable("id") Long id
    ) {

        return ResponseEntity.ok(
                fraudDetectionService.getAssessmentById(id)
        );
    }

    @GetMapping("/transactions/{transactionReference}")
    public ResponseEntity<List<FraudAssessmentResponse>>
    getByTransaction(
            @PathVariable("transactionReference")
            String transactionReference
    ) {

        return ResponseEntity.ok(
                fraudDetectionService
                        .getAssessmentsByTransaction(
                                transactionReference
                        )
        );
    }

    @GetMapping("/high-risk")
    public ResponseEntity<List<FraudAssessmentResponse>>
    getHighRiskAssessments() {

        return ResponseEntity.ok(
                fraudDetectionService
                        .getHighRiskAssessments()
        );
    }

    @GetMapping("/pending-review")
    public ResponseEntity<List<FraudAssessmentResponse>>
    getPendingReviews() {

        return ResponseEntity.ok(
                fraudDetectionService
                        .getPendingReviews()
        );
    }

    @PatchMapping("/assessments/{id}/review")
    public ResponseEntity<FraudAssessmentResponse> reviewAssessment(
            @PathVariable("id") Long id,
            @Valid @RequestBody FraudReviewRequest request,
            @AuthenticationPrincipal Jwt jwt
    ) {

        Long reviewerUserId =
                jwt.getClaim("userId");

        return ResponseEntity.ok(
                fraudDetectionService.reviewAssessment(
                        id,
                        request.reviewStatus(),
                        request.reviewNotes(),
                        reviewerUserId
                )
        );
    }
}
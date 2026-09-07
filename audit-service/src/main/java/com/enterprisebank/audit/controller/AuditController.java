package com.enterprisebank.audit.controller;

import com.enterprisebank.audit.dto.AuditEventResponse;
import com.enterprisebank.audit.service.AuditEventService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/audit")
@RequiredArgsConstructor
public class AuditController {

    private final AuditEventService auditEventService;

    @GetMapping("/events")
    public ResponseEntity<List<AuditEventResponse>>
    getAllEvents() {

        return ResponseEntity.ok(
                auditEventService.getAllAuditEvents()
        );
    }

    @GetMapping("/events/{eventId}")
    public ResponseEntity<AuditEventResponse>
    getByEventId(
            @PathVariable("eventId") String eventId
    ) {

        return ResponseEntity.ok(
                auditEventService.getByEventId(eventId)
        );
    }

    @GetMapping("/transactions/{transactionReference}")
    public ResponseEntity<List<AuditEventResponse>>
    getByTransactionReference(
            @PathVariable("transactionReference")
            String transactionReference
    ) {

        return ResponseEntity.ok(
                auditEventService
                        .getByTransactionReference(
                                transactionReference
                        )
        );
    }
}
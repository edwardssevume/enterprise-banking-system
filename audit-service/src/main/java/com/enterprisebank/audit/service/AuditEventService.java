package com.enterprisebank.audit.service;

import com.enterprisebank.audit.dto.AuditEventResponse;
import com.enterprisebank.audit.entity.AuditEvent;
import com.enterprisebank.audit.event.TransactionCompletedEvent;
import com.enterprisebank.audit.repository.AuditEventRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
@Slf4j
public class AuditEventService {

    private final AuditEventRepository auditEventRepository;

    @Transactional
    public void recordTransactionEvent(
            TransactionCompletedEvent event
    ) {

        if (auditEventRepository.existsByEventId(event.eventId())) {

            log.info(
                    "Audit event already processed. eventId={}",
                    event.eventId()
            );

            return;
        }

        AuditEvent auditEvent =
                AuditEvent.builder()
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
                        .amount(
                                event.amount()
                        )
                        .currency(
                                event.currency()
                        )
                        .description(
                                event.description()
                        )
                        .initiatedByUserId(
                                event.initiatedByUserId()
                        )
                        .transactionCompletedAt(
                                event.completedAt()
                        )
                        .build();

        auditEventRepository.save(auditEvent);

        log.info(
                "Transaction audit event recorded. eventId={}, reference={}",
                event.eventId(),
                event.transactionReference()
        );
    }

    @Transactional(readOnly = true)
    public List<AuditEventResponse> getAllAuditEvents() {

        return auditEventRepository
                .findAll(
                        Sort.by(
                                Sort.Direction.DESC,
                                "auditedAt"
                        )
                )
                .stream()
                .map(this::mapToResponse)
                .toList();
    }

    @Transactional(readOnly = true)
    public AuditEventResponse getByEventId(
            String eventId
    ) {

        AuditEvent auditEvent =
                auditEventRepository
                        .findByEventId(eventId)
                        .orElseThrow(
                                () -> new RuntimeException(
                                        "Audit event not found: "
                                                + eventId
                                )
                        );

        return mapToResponse(auditEvent);
    }

    @Transactional(readOnly = true)
    public List<AuditEventResponse> getByTransactionReference(
            String transactionReference
    ) {

        return auditEventRepository
                .findByTransactionReferenceOrderByAuditedAtDesc(
                        transactionReference
                )
                .stream()
                .map(this::mapToResponse)
                .toList();
    }

    private AuditEventResponse mapToResponse(
            AuditEvent auditEvent
    ) {

        return new AuditEventResponse(
                auditEvent.getId(),
                auditEvent.getEventId(),
                auditEvent.getTransactionReference(),
                auditEvent.getTransactionType(),
                auditEvent.getSourceAccountId(),
                auditEvent.getDestinationAccountId(),
                auditEvent.getAmount(),
                auditEvent.getCurrency(),
                auditEvent.getDescription(),
                auditEvent.getInitiatedByUserId(),
                auditEvent.getTransactionCompletedAt(),
                auditEvent.getAuditedAt()
        );
    }
}
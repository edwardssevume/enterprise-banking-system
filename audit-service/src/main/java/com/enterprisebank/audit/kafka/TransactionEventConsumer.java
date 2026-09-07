package com.enterprisebank.audit.kafka;

import com.enterprisebank.audit.event.TransactionCompletedEvent;
import com.enterprisebank.audit.service.AuditEventService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
@Slf4j
public class TransactionEventConsumer {

    private final AuditEventService auditEventService;

    @KafkaListener(
            topics = "transaction-events",
            groupId = "audit-service-group"
    )
    public void consume(TransactionCompletedEvent event) {

        log.info(
                "Received transaction event. eventId={}, reference={}, type={}",
                event.eventId(),
                event.transactionReference(),
                event.transactionType()
        );

        auditEventService.recordTransactionEvent(event);
    }
}
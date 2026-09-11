package com.enterprisebank.fraud.kafka;

import com.enterprisebank.fraud.event.TransactionCompletedEvent;
import com.enterprisebank.fraud.service.FraudDetectionService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
@Slf4j
public class TransactionEventConsumer {

    private final FraudDetectionService fraudDetectionService;

    @KafkaListener(
            topics = "transaction-events",
            groupId = "fraud-service-group"
    )
    public void consume(
            TransactionCompletedEvent event
    ) {

        log.info(
                "Received transaction for fraud assessment. eventId={}, reference={}, amount={}",
                event.eventId(),
                event.transactionReference(),
                event.amount()
        );

        fraudDetectionService.assessTransaction(event);
    }
}
package com.enterprisebank.transaction.kafka;

import com.enterprisebank.transaction.event.TransactionCompletedEvent;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
@Slf4j
public class TransactionEventProducer {

    private static final String TRANSACTION_EVENTS_TOPIC =
            "transaction-events";

    private final KafkaTemplate<String, TransactionCompletedEvent> kafkaTemplate;

    public void publish(TransactionCompletedEvent event) {

        log.info(
                "Publishing transaction event. reference={}, type={}, eventId={}",
                event.transactionReference(),
                event.transactionType(),
                event.eventId()
        );

        kafkaTemplate.send(
                TRANSACTION_EVENTS_TOPIC,
                event.transactionReference(),
                event
        );
    }
}
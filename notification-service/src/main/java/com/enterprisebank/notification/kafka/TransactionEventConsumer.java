package com.enterprisebank.notification.kafka;

import com.enterprisebank.notification.event.TransactionCompletedEvent;
import com.enterprisebank.notification.service.NotificationService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
@Slf4j
public class TransactionEventConsumer {

    private final NotificationService notificationService;

    @KafkaListener(
            topics = "transaction-events",
            groupId = "notification-service-group"
    )
    public void consume(
            TransactionCompletedEvent event
    ) {

        log.info(
                "Received transaction event for notification. eventId={}, reference={}, type={}",
                event.eventId(),
                event.transactionReference(),
                event.transactionType()
        );

        notificationService.processTransactionEvent(event);
    }
}
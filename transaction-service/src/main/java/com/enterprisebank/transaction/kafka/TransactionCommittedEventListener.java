package com.enterprisebank.transaction.kafka;

import com.enterprisebank.transaction.entity.BankTransaction;
import com.enterprisebank.transaction.event.TransactionCommittedEvent;
import com.enterprisebank.transaction.event.TransactionCompletedEvent;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import org.springframework.transaction.event.TransactionPhase;
import org.springframework.transaction.event.TransactionalEventListener;

import java.util.UUID;

@Component
@RequiredArgsConstructor
@Slf4j
public class TransactionCommittedEventListener {

    private final TransactionEventProducer transactionEventProducer;

    @TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT)
    public void handleTransactionCommitted(
            TransactionCommittedEvent event
    ) {

        BankTransaction transaction = event.transaction();

        TransactionCompletedEvent kafkaEvent =
                new TransactionCompletedEvent(
                        UUID.randomUUID().toString(),
                        transaction.getTransactionReference(),
                        transaction.getTransactionType(),
                        transaction.getSourceAccountId(),
                        transaction.getDestinationAccountId(),
                        transaction.getAmount(),
                        transaction.getCurrency(),
                        transaction.getDescription(),
                        transaction.getInitiatedByUserId(),
                        transaction.getCompletedAt()
                );

        log.info(
                "Database transaction committed. Publishing Kafka event. reference={}",
                transaction.getTransactionReference()
        );

        transactionEventProducer.publish(kafkaEvent);
    }
}
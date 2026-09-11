package com.enterprisebank.fraud.integration;

import com.enterprisebank.fraud.entity.FraudAssessment;
import com.enterprisebank.fraud.entity.ReviewStatus;
import com.enterprisebank.fraud.entity.RiskLevel;
import com.enterprisebank.fraud.event.TransactionCompletedEvent;
import com.enterprisebank.fraud.repository.FraudAssessmentRepository;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;
import org.testcontainers.kafka.KafkaContainer;
import org.testcontainers.mysql.MySQLContainer;

import java.math.BigDecimal;
import java.time.Duration;
import java.time.LocalDateTime;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.awaitility.Awaitility.await;

@SpringBootTest
@Testcontainers
class FraudServiceIntegrationTest {

    @Container
    static final MySQLContainer mysql =
            new MySQLContainer("mysql:8.4")
                    .withDatabaseName("fraud_test_db")
                    .withUsername("test")
                    .withPassword("test");

    @Container
    static final KafkaContainer kafka =
            new KafkaContainer("apache/kafka-native:3.8.0");

    @Autowired
    private KafkaTemplate<String, Object> kafkaTemplate;

    @Autowired
    private FraudAssessmentRepository fraudAssessmentRepository;

    @DynamicPropertySource
    static void configureProperties(
            DynamicPropertyRegistry registry
    ) {

        registry.add(
                "spring.datasource.url",
                mysql::getJdbcUrl
        );

        registry.add(
                "spring.datasource.username",
                mysql::getUsername
        );

        registry.add(
                "spring.datasource.password",
                mysql::getPassword
        );

        registry.add(
                "spring.kafka.bootstrap-servers",
                kafka::getBootstrapServers
        );

        registry.add(
                "spring.kafka.producer.key-serializer",
                () -> "org.apache.kafka.common.serialization.StringSerializer"
        );

        registry.add(
                "spring.kafka.producer.value-serializer",
                () -> "org.springframework.kafka.support.serializer.JsonSerializer"
        );

        registry.add(
                "spring.kafka.consumer.group-id",
                () -> "fraud-service-test-group"
        );

        registry.add(
                "spring.kafka.consumer.auto-offset-reset",
                () -> "earliest"
        );

        registry.add(
                "eureka.client.enabled",
                () -> false
        );

        registry.add(
                "spring.cloud.config.enabled",
                () -> false
        );

        registry.add(
                "jwt.secret",
                () ->
                        "VGhpc0lzQVRlc3RTZWNyZXRLZXlGb3JKV1RUZXN0aW5nMTIzNDU2"
        );

        registry.add(
                "spring.kafka.consumer.key-deserializer",
                () -> "org.springframework.kafka.support.serializer.ErrorHandlingDeserializer"
        );

        registry.add(
                "spring.kafka.consumer.value-deserializer",
                () -> "org.springframework.kafka.support.serializer.ErrorHandlingDeserializer"
        );

        registry.add(
                "spring.kafka.consumer.properties.spring.deserializer.key.delegate.class",
                () -> "org.apache.kafka.common.serialization.StringDeserializer"
        );

        registry.add(
                "spring.kafka.consumer.properties.spring.deserializer.value.delegate.class",
                () -> "org.springframework.kafka.support.serializer.JsonDeserializer"
        );

        registry.add(
                "spring.kafka.consumer.properties.spring.json.trusted.packages",
                () -> "com.enterprisebank.fraud.event"
        );

        registry.add(
                "spring.kafka.consumer.properties.spring.json.use.type.headers",
                () -> false
        );

        registry.add(
                "spring.kafka.consumer.properties.spring.json.value.default.type",
                () -> "com.enterprisebank.fraud.event.TransactionCompletedEvent"
        );

        registry.add(
                "spring.kafka.producer.properties.spring.json.add.type.headers",
                () -> false
        );
    }

    @Test
    void shouldConsumeHighValueTransactionAndPersistHighRiskAssessment()
            throws Exception {

        String eventId =
                UUID.randomUUID().toString();

        String transactionReference =
                "TXN-TEST-" + UUID.randomUUID();

        TransactionCompletedEvent event =
                new TransactionCompletedEvent(
                        eventId,
                        transactionReference,
                        "DEPOSIT",
                        null,
                        1L,
                        new BigDecimal("15000.00"),
                        "CAD",
                        "Integration test",
                        1L,
                        LocalDateTime.now()
                );

        kafkaTemplate
                .send(
                        "transaction-events",
                        transactionReference,
                        event
                )
                .get();

        await()
                .atMost(Duration.ofSeconds(30))
                .pollInterval(Duration.ofMillis(500))
                .untilAsserted(() -> {

                    var result =
                            fraudAssessmentRepository
                                    .findByEventId(eventId);

                    assertThat(result)
                            .as("Fraud assessment should eventually be persisted")
                            .isPresent();

                    FraudAssessment assessment =
                            result.get();

                    assertThat(
                            assessment.getRiskLevel()
                    ).isEqualTo(RiskLevel.HIGH);

                    assertThat(
                            assessment.getRiskScore()
                    ).isEqualTo(90);

                    assertThat(
                            assessment.getReviewStatus()
                    ).isEqualTo(
                            ReviewStatus.PENDING_REVIEW
                    );
                });
    }
}
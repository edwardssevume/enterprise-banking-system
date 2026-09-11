package com.enterprisebank.fraud.integration;

import com.enterprisebank.fraud.FraudServiceApplication;
import com.enterprisebank.fraud.entity.FraudAssessment;
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

@Testcontainers
@SpringBootTest(
        classes = FraudServiceApplication.class,
        properties = {
                "spring.cloud.config.enabled=false",
                "eureka.client.enabled=false",
                "eureka.client.register-with-eureka=false",
                "eureka.client.fetch-registry=false"
        }
)
class FraudServiceIntegrationTest {

    @Container
    static final MySQLContainer MYSQL =
            new MySQLContainer("mysql:8.4")
                    .withDatabaseName("fraud_test_db")
                    .withUsername("test")
                    .withPassword("test");

    @Container
    static final KafkaContainer KAFKA =
            new KafkaContainer("apache/kafka-native:3.8.0");

    @Autowired
    private KafkaTemplate<String, TransactionCompletedEvent> kafkaTemplate;

    @Autowired
    private FraudAssessmentRepository fraudAssessmentRepository;

    @DynamicPropertySource
    static void configureProperties(DynamicPropertyRegistry registry) {

        // -----------------------------
        // MySQL Testcontainer
        // -----------------------------

        registry.add(
                "spring.datasource.url",
                MYSQL::getJdbcUrl
        );

        registry.add(
                "spring.datasource.username",
                MYSQL::getUsername
        );

        registry.add(
                "spring.datasource.password",
                MYSQL::getPassword
        );

        registry.add(
                "spring.datasource.driver-class-name",
                MYSQL::getDriverClassName
        );

        registry.add(
                "spring.jpa.hibernate.ddl-auto",
                () -> "create-drop"
        );

        // -----------------------------
        // Kafka Testcontainer
        // -----------------------------

        registry.add(
                "spring.kafka.bootstrap-servers",
                KAFKA::getBootstrapServers
        );

        // Producer
        registry.add(
                "spring.kafka.producer.key-serializer",
                () -> "org.apache.kafka.common.serialization.StringSerializer"
        );

        registry.add(
                "spring.kafka.producer.value-serializer",
                () -> "org.springframework.kafka.support.serializer.JsonSerializer"
        );

        registry.add(
                "spring.kafka.producer.properties.spring.json.add.type.headers",
                () -> "false"
        );

        // Consumer
        registry.add(
                "spring.kafka.consumer.group-id",
                () -> "fraud-service-integration-test-group"
        );

        registry.add(
                "spring.kafka.consumer.auto-offset-reset",
                () -> "earliest"
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
                () -> "false"
        );

        registry.add(
                "spring.kafka.consumer.properties.spring.json.value.default.type",
                () -> "com.enterprisebank.fraud.event.TransactionCompletedEvent"
        );
    }

    @Test
    void shouldConsumeHighValueTransactionAndPersistHighRiskAssessment()
            throws Exception {

        String eventId = UUID.randomUUID().toString();

        String transactionReference =
                "TXN-TEST-" + UUID.randomUUID();

        TransactionCompletedEvent event =
                new TransactionCompletedEvent(
                        eventId,
                        transactionReference,
                        "TRANSFER",
                        1001L,
                        2001L,
                        new BigDecimal("15000.00"),
                        "CAD",
                        "High value integration test transaction",
                        1L,
                        LocalDateTime.now()
                );

        // Publish test transaction to Kafka
        kafkaTemplate
                .send(
                        "transaction-events",
                        transactionReference,
                        event
                )
                .get();

        // Wait until Fraud Service consumes and persists it
        await()
                .atMost(Duration.ofSeconds(20))
                .until(() ->
                        fraudAssessmentRepository
                                .findByEventId(eventId)
                                .isPresent()
                );

        FraudAssessment assessment =
                fraudAssessmentRepository
                        .findByEventId(eventId)
                        .orElseThrow();

        // Verify persisted assessment
        assertThat(assessment.getEventId())
                .isEqualTo(eventId);

        assertThat(assessment.getTransactionReference())
                .isEqualTo(transactionReference);

        assertThat(assessment.getAmount())
                .isEqualByComparingTo("15000.00");

        assertThat(assessment.getCurrency())
                .isEqualTo("CAD");

        assertThat(assessment.getTransactionType())
                .isEqualTo("TRANSFER");

        assertThat(assessment.getRiskLevel())
                .isEqualTo(RiskLevel.HIGH);

        assertThat(assessment.getRiskScore())
                .isNotNull();

        assertThat(assessment.getFraudReason())
                .isNotBlank();

        assertThat(assessment.getAssessedAt())
                .isNotNull();
    }
}
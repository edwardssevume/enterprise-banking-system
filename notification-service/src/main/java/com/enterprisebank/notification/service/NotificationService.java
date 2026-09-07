package com.enterprisebank.notification.service;

import com.enterprisebank.notification.dto.NotificationResponse;
import com.enterprisebank.notification.entity.Notification;
import com.enterprisebank.notification.event.TransactionCompletedEvent;
import com.enterprisebank.notification.exception.NotificationAccessDeniedException;
import com.enterprisebank.notification.repository.NotificationRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Sort;
import org.springframework.security.core.Authentication;
import org.springframework.security.oauth2.server.resource.authentication.JwtAuthenticationToken;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
@Slf4j
public class NotificationService {

    private final NotificationRepository notificationRepository;

    // =====================================================
    // KAFKA EVENT PROCESSING
    // =====================================================

    @Transactional
    public void processTransactionEvent(
            TransactionCompletedEvent event
    ) {

        if (notificationRepository.existsByEventId(event.eventId())) {

            log.info(
                    "Notification already processed. eventId={}",
                    event.eventId()
            );

            return;
        }

        String message = buildMessage(event);

        Notification notification =
                Notification.builder()
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
                        .amount(event.amount())
                        .currency(event.currency())
                        .description(event.description())
                        .initiatedByUserId(
                                event.initiatedByUserId()
                        )
                        .transactionCompletedAt(
                                event.completedAt()
                        )
                        .notificationMessage(message)
                        .build();

        notificationRepository.save(notification);

        log.info(
                "Notification recorded. eventId={}, reference={}",
                event.eventId(),
                event.transactionReference()
        );
    }

    // =====================================================
    // ADMIN / EMPLOYEE - GET ALL
    // =====================================================

    @Transactional(readOnly = true)
    public List<NotificationResponse> getAllNotifications() {

        return notificationRepository
                .findAll(
                        Sort.by(
                                Sort.Direction.DESC,
                                "createdAt"
                        )
                )
                .stream()
                .map(this::mapToResponse)
                .toList();
    }

    // =====================================================
    // GET NOTIFICATION BY ID WITH OWNERSHIP CHECK
    // =====================================================

    @Transactional(readOnly = true)
    public NotificationResponse getNotificationById(
            Long id,
            Authentication authentication
    ) {

        Notification notification =
                notificationRepository
                        .findById(id)
                        .orElseThrow(
                                () -> new RuntimeException(
                                        "Notification not found: " + id
                                )
                        );

        verifyAccess(
                notification.getInitiatedByUserId(),
                authentication
        );

        return mapToResponse(notification);
    }

    // =====================================================
    // GET USER NOTIFICATIONS
    // =====================================================

    @Transactional(readOnly = true)
    public List<NotificationResponse> getNotificationsByUser(
            Long requestedUserId,
            Authentication authentication
    ) {

        if (isEmployeeOrAdmin(authentication)) {

            return notificationRepository
                    .findByInitiatedByUserIdOrderByCreatedAtDesc(
                            requestedUserId
                    )
                    .stream()
                    .map(this::mapToResponse)
                    .toList();
        }

        Long loggedInUserId =
                extractUserId(authentication);

        if (!loggedInUserId.equals(requestedUserId)) {

            throw new NotificationAccessDeniedException(
                    "You cannot access another user's notifications"
            );
        }

        return notificationRepository
                .findByInitiatedByUserIdOrderByCreatedAtDesc(
                        loggedInUserId
                )
                .stream()
                .map(this::mapToResponse)
                .toList();
    }

    // =====================================================
    // GET TRANSACTION NOTIFICATIONS WITH OWNERSHIP CHECK
    // =====================================================

    @Transactional(readOnly = true)
    public List<NotificationResponse>
    getNotificationsByTransaction(
            String transactionReference,
            Authentication authentication
    ) {

        List<Notification> notifications =
                notificationRepository
                        .findByTransactionReferenceOrderByCreatedAtDesc(
                                transactionReference
                        );

        if (isEmployeeOrAdmin(authentication)) {

            return notifications
                    .stream()
                    .map(this::mapToResponse)
                    .toList();
        }

        Long loggedInUserId =
                extractUserId(authentication);

        return notifications
                .stream()
                .filter(notification ->
                        loggedInUserId.equals(
                                notification.getInitiatedByUserId()
                        )
                )
                .map(this::mapToResponse)
                .toList();
    }

    // =====================================================
    // OWNERSHIP CHECK
    // =====================================================

    private void verifyAccess(
            Long notificationUserId,
            Authentication authentication
    ) {

        if (isEmployeeOrAdmin(authentication)) {
            return;
        }

        Long loggedInUserId =
                extractUserId(authentication);

        if (!loggedInUserId.equals(notificationUserId)) {

            throw new NotificationAccessDeniedException(
                    "You cannot access this notification"
            );
        }
    }

    // =====================================================
    // ADMIN / EMPLOYEE CHECK
    // =====================================================

    private boolean isEmployeeOrAdmin(
            Authentication authentication
    ) {

        return authentication
                .getAuthorities()
                .stream()
                .anyMatch(authority ->
                        authority.getAuthority()
                                .equals("ROLE_ADMIN")
                                ||
                                authority.getAuthority()
                                        .equals("ROLE_EMPLOYEE")
                );
    }

    // =====================================================
    // GET userId FROM JWT
    // =====================================================

    private Long extractUserId(
            Authentication authentication
    ) {

        if (!(authentication
                instanceof JwtAuthenticationToken jwtToken)) {

            throw new NotificationAccessDeniedException(
                    "Invalid authentication"
            );
        }

        Object userIdClaim =
                jwtToken
                        .getToken()
                        .getClaim("userId");

        if (userIdClaim == null) {

            throw new NotificationAccessDeniedException(
                    "JWT does not contain userId"
            );
        }

        return Long.valueOf(
                userIdClaim.toString()
        );
    }

    // =====================================================
    // BUILD MESSAGE
    // =====================================================

    private String buildMessage(
            TransactionCompletedEvent event
    ) {

        return switch (event.transactionType()) {

            case "DEPOSIT" ->
                    String.format(
                            "Deposit of %s %s completed successfully. Reference: %s",
                            event.amount(),
                            event.currency(),
                            event.transactionReference()
                    );

            case "WITHDRAWAL" ->
                    String.format(
                            "Withdrawal of %s %s completed successfully. Reference: %s",
                            event.amount(),
                            event.currency(),
                            event.transactionReference()
                    );

            case "TRANSFER" ->
                    String.format(
                            "Transfer of %s %s completed successfully. Reference: %s",
                            event.amount(),
                            event.currency(),
                            event.transactionReference()
                    );

            default ->
                    String.format(
                            "Transaction %s completed successfully. Reference: %s",
                            event.transactionType(),
                            event.transactionReference()
                    );
        };
    }

    // =====================================================
    // ENTITY -> DTO
    // =====================================================

    private NotificationResponse mapToResponse(
            Notification notification
    ) {

        return new NotificationResponse(
                notification.getId(),
                notification.getEventId(),
                notification.getTransactionReference(),
                notification.getTransactionType(),
                notification.getSourceAccountId(),
                notification.getDestinationAccountId(),
                notification.getAmount(),
                notification.getCurrency(),
                notification.getDescription(),
                notification.getInitiatedByUserId(),
                notification.getNotificationMessage(),
                notification.getTransactionCompletedAt(),
                notification.getCreatedAt()
        );
    }
}
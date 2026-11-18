package com.loyalty.points.service;

import com.loyalty.common.constants.KafkaTopicConstants;
import com.loyalty.common.events.LoyaltyPointsAwardedEvent;
import com.loyalty.common.events.RewardRedemptionCompletedEvent;
import com.loyalty.common.events.RewardRedemptionRequestedEvent;
import com.loyalty.common.events.TransactionCreatedEvent;
import com.loyalty.points.entity.ProcessedEvent;
import com.loyalty.points.repository.ProcessedEventRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;

@Service
@RequiredArgsConstructor
@Slf4j
public class LoyaltyKafkaConsumerService {

    private final LoyaltyAccountService accountService;
    private final ProcessedEventRepository processedEventRepository;
    private final LoyaltyKafkaProducerService producerService;

    @KafkaListener(
            topics = KafkaTopicConstants.TRANSACTION_EVENTS_TOPIC,
            groupId = KafkaTopicConstants.LOYALTY_SERVICE_GROUP,
            containerFactory = "kafkaListenerContainerFactory"
    )
    @Transactional
    public void consumeTransactionCreatedEvent(TransactionCreatedEvent event) {
        log.info("Received TransactionCreatedEvent: eventId={}, transactionRef={}", event.getEventId(), event.getTransactionReference());

        // Duplicate event protection (Idempotency)
        if (processedEventRepository.existsById(event.getEventId())) {
            log.warn("Duplicate event detected. Skipping eventId={}", event.getEventId());
            return;
        }

        try {
            var awardResult = accountService.awardPoints(
                    event.getCustomerId(),
                    event.getCustomerNumber(),
                    event.getAmount(),
                    event.getTransactionReference(),
                    "Points awarded for " + event.getMerchantName() + " purchase"
            );

            // Record processed event
            processedEventRepository.save(ProcessedEvent.builder()
                    .eventId(event.getEventId())
                    .eventType(event.getEventType())
                    .processedAt(LocalDateTime.now())
                    .build());

            // Publish points awarded event
            LoyaltyPointsAwardedEvent awardedEvent = LoyaltyPointsAwardedEvent.builder()
                    .correlationId(event.getCorrelationId())
                    .transactionId(event.getTransactionId())
                    .transactionReference(event.getTransactionReference())
                    .customerId(event.getCustomerId())
                    .customerNumber(event.getCustomerNumber())
                    .transactionAmount(event.getAmount())
                    .pointsAwarded(awardResult.pointsAwarded())
                    .newPointsBalance(awardResult.newBalance())
                    .lifetimePoints(awardResult.lifetimePoints())
                    .tierLevel(awardResult.tierLevel())
                    .tierUpgraded(awardResult.tierUpgraded())
                    .build();

            producerService.publishPointsAwardedEvent(awardedEvent);

        } catch (Exception ex) {
            log.error("Error processing TransactionCreatedEvent id={}: {}", event.getEventId(), ex.getMessage(), ex);
            throw ex; // Re-throw for Kafka retry / DLQ handling
        }
    }

    @KafkaListener(
            topics = KafkaTopicConstants.REDEMPTION_REQUEST_TOPIC,
            groupId = KafkaTopicConstants.LOYALTY_SERVICE_GROUP,
            containerFactory = "kafkaListenerContainerFactory"
    )
    @Transactional
    public void consumeRedemptionRequestedEvent(RewardRedemptionRequestedEvent event) {
        log.info("Received RewardRedemptionRequestedEvent: eventId={}, redemptionId={}", event.getEventId(), event.getRedemptionId());

        if (processedEventRepository.existsById(event.getEventId())) {
            log.warn("Duplicate redemption request detected. Skipping eventId={}", event.getEventId());
            return;
        }

        try {
            var deductResult = accountService.deductPoints(
                    event.getCustomerId(),
                    event.getPointsRequired(),
                    "RED-" + event.getRedemptionId(),
                    "Redemption for: " + event.getRewardTitle()
            );

            processedEventRepository.save(ProcessedEvent.builder()
                    .eventId(event.getEventId())
                    .eventType(event.getEventType())
                    .processedAt(LocalDateTime.now())
                    .build());

            RewardRedemptionCompletedEvent completedEvent = RewardRedemptionCompletedEvent.builder()
                    .correlationId(event.getCorrelationId())
                    .redemptionId(event.getRedemptionId())
                    .customerId(event.getCustomerId())
                    .rewardId(event.getRewardId())
                    .pointsDeducted(deductResult.success() ? event.getPointsRequired() : 0L)
                    .status(deductResult.success() ? "SUCCESS" : "FAILED")
                    .failureReason(deductResult.errorMessage())
                    .remainingBalance(deductResult.remainingBalance())
                    .build();

            producerService.publishRedemptionCompletedEvent(completedEvent);

        } catch (Exception ex) {
            log.error("Error processing RewardRedemptionRequestedEvent id={}: {}", event.getEventId(), ex.getMessage(), ex);
            RewardRedemptionCompletedEvent errorEvent = RewardRedemptionCompletedEvent.builder()
                    .correlationId(event.getCorrelationId())
                    .redemptionId(event.getRedemptionId())
                    .customerId(event.getCustomerId())
                    .rewardId(event.getRewardId())
                    .pointsDeducted(0L)
                    .status("FAILED")
                    .failureReason(ex.getMessage())
                    .build();
            producerService.publishRedemptionCompletedEvent(errorEvent);
        }
    }
}

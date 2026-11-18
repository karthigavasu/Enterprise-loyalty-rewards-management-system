package com.loyalty.transaction.service;

import com.loyalty.common.constants.KafkaTopicConstants;
import com.loyalty.common.events.LoyaltyPointsAwardedEvent;
import com.loyalty.transaction.entity.ProcessedEvent;
import com.loyalty.transaction.repository.ProcessedEventRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;

@Service
@RequiredArgsConstructor
@Slf4j
public class TransactionKafkaConsumerService {

    private final CustomerTransactionService transactionService;
    private final ProcessedEventRepository processedEventRepository;

    @KafkaListener(
            topics = KafkaTopicConstants.LOYALTY_EVENTS_TOPIC,
            groupId = KafkaTopicConstants.TRANSACTION_SERVICE_GROUP,
            containerFactory = "kafkaListenerContainerFactory"
    )
    @Transactional
    public void consumeLoyaltyPointsAwardedEvent(LoyaltyPointsAwardedEvent event) {
        log.info("Received LoyaltyPointsAwardedEvent: eventId={}, txnId={}, pointsAwarded={}",
                event.getEventId(), event.getTransactionId(), event.getPointsAwarded());

        if (processedEventRepository.existsById(event.getEventId())) {
            log.warn("Duplicate points awarded event received. Skipping eventId={}", event.getEventId());
            return;
        }

        try {
            if (event.getTransactionId() != null) {
                transactionService.markPointsAwarded(event.getTransactionId(), event.getPointsAwarded());
            }

            processedEventRepository.save(ProcessedEvent.builder()
                    .eventId(event.getEventId())
                    .eventType(event.getEventType())
                    .processedAt(LocalDateTime.now())
                    .build());

        } catch (Exception ex) {
            log.error("Failed to process LoyaltyPointsAwardedEvent: {}", ex.getMessage(), ex);
        }
    }
}

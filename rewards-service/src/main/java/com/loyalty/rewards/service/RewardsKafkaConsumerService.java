package com.loyalty.rewards.service;

import com.loyalty.common.constants.KafkaTopicConstants;
import com.loyalty.common.events.RewardRedemptionCompletedEvent;
import com.loyalty.rewards.entity.ProcessedEvent;
import com.loyalty.rewards.repository.ProcessedEventRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;

@Service
@RequiredArgsConstructor
@Slf4j
public class RewardsKafkaConsumerService {

    private final RedemptionService redemptionService;
    private final ProcessedEventRepository processedEventRepository;

    @KafkaListener(
            topics = KafkaTopicConstants.REDEMPTION_RESULT_TOPIC,
            groupId = KafkaTopicConstants.REWARDS_SERVICE_GROUP,
            containerFactory = "kafkaListenerContainerFactory"
    )
    @Transactional
    public void consumeRedemptionCompletedEvent(RewardRedemptionCompletedEvent event) {
        log.info("Received RewardRedemptionCompletedEvent: eventId={}, redemptionId={}, status={}",
                event.getEventId(), event.getRedemptionId(), event.getStatus());

        if (processedEventRepository.existsById(event.getEventId())) {
            log.warn("Duplicate completion event received. Skipping eventId={}", event.getEventId());
            return;
        }

        try {
            redemptionService.handleRedemptionResult(event);

            processedEventRepository.save(ProcessedEvent.builder()
                    .eventId(event.getEventId())
                    .eventType(event.getEventType())
                    .processedAt(LocalDateTime.now())
                    .build());

        } catch (Exception ex) {
            log.error("Failed to handle RewardRedemptionCompletedEvent: {}", ex.getMessage(), ex);
        }
    }
}

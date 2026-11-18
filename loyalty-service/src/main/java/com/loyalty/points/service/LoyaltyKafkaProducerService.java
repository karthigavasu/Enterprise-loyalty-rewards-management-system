package com.loyalty.points.service;

import com.loyalty.common.constants.KafkaTopicConstants;
import com.loyalty.common.events.LoyaltyPointsAwardedEvent;
import com.loyalty.common.events.RewardRedemptionCompletedEvent;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
@Slf4j
public class LoyaltyKafkaProducerService {

    private final KafkaTemplate<String, Object> kafkaTemplate;

    public void publishPointsAwardedEvent(LoyaltyPointsAwardedEvent event) {
        try {
            log.info("Publishing LoyaltyPointsAwardedEvent for customer {}: points = {}",
                    event.getCustomerId(), event.getPointsAwarded());
            kafkaTemplate.send(KafkaTopicConstants.LOYALTY_EVENTS_TOPIC, event.getCustomerId().toString(), event);
        } catch (Exception ex) {
            log.error("Failed to publish LoyaltyPointsAwardedEvent: {}", ex.getMessage(), ex);
        }
    }

    public void publishRedemptionCompletedEvent(RewardRedemptionCompletedEvent event) {
        try {
            log.info("Publishing RewardRedemptionCompletedEvent for redemption {}: status = {}",
                    event.getRedemptionId(), event.getStatus());
            kafkaTemplate.send(KafkaTopicConstants.REDEMPTION_RESULT_TOPIC, event.getRedemptionId().toString(), event);
        } catch (Exception ex) {
            log.error("Failed to publish RewardRedemptionCompletedEvent: {}", ex.getMessage(), ex);
        }
    }
}

package com.loyalty.rewards.service;

import com.loyalty.common.constants.KafkaTopicConstants;
import com.loyalty.common.events.RewardRedemptionRequestedEvent;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
@Slf4j
public class RewardsKafkaProducerService {

    private final KafkaTemplate<String, Object> kafkaTemplate;

    public void publishRedemptionRequestedEvent(RewardRedemptionRequestedEvent event) {
        try {
            log.info("Publishing RewardRedemptionRequestedEvent for redemptionId = {}", event.getRedemptionId());
            kafkaTemplate.send(KafkaTopicConstants.REDEMPTION_REQUEST_TOPIC, event.getRedemptionId().toString(), event);
        } catch (Exception ex) {
            log.error("Failed to publish RewardRedemptionRequestedEvent: {}", ex.getMessage());
        }
    }
}

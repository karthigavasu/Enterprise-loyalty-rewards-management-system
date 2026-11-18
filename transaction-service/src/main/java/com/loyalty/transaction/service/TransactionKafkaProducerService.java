package com.loyalty.transaction.service;

import com.loyalty.common.constants.KafkaTopicConstants;
import com.loyalty.common.events.TransactionCreatedEvent;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
@Slf4j
public class TransactionKafkaProducerService {

    private final KafkaTemplate<String, Object> kafkaTemplate;

    public void publishTransactionCreatedEvent(TransactionCreatedEvent event) {
        try {
            log.info("Publishing TransactionCreatedEvent for txn ref: {}, customer: {}",
                    event.getTransactionReference(), event.getCustomerId());
            kafkaTemplate.send(KafkaTopicConstants.TRANSACTION_EVENTS_TOPIC, event.getTransactionReference(), event);
        } catch (Exception ex) {
            log.error("Failed to publish TransactionCreatedEvent: {}", ex.getMessage());
        }
    }
}

package com.loyalty.common.constants;

public final class KafkaTopicConstants {
    private KafkaTopicConstants() {}

    public static final String TRANSACTION_EVENTS_TOPIC = "transaction-events";
    public static final String LOYALTY_EVENTS_TOPIC = "loyalty-events";
    public static final String REDEMPTION_REQUEST_TOPIC = "redemption-request-events";
    public static final String REDEMPTION_RESULT_TOPIC = "redemption-result-events";

    public static final String LOYALTY_SERVICE_GROUP = "loyalty-service-group";
    public static final String REWARDS_SERVICE_GROUP = "rewards-service-group";
    public static final String TRANSACTION_SERVICE_GROUP = "transaction-service-group";
}

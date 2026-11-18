package com.loyalty.common.events;

import com.loyalty.common.enums.PaymentMethod;
import com.loyalty.common.enums.TransactionCategory;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.UUID;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class TransactionCreatedEvent implements Serializable {
    @Builder.Default
    private String eventId = UUID.randomUUID().toString();
    @Builder.Default
    private String eventType = "TransactionCreatedEvent";
    private String correlationId;
    
    private Long transactionId;
    private String transactionReference;
    private Long customerId;
    private String customerNumber;
    private BigDecimal amount;
    private String currency;
    private String merchantName;
    private String storeLocation;
    private TransactionCategory category;
    private PaymentMethod paymentMethod;
    
    @Builder.Default
    private LocalDateTime timestamp = LocalDateTime.now();
}

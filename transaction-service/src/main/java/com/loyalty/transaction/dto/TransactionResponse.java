package com.loyalty.transaction.dto;

import com.loyalty.common.enums.PaymentMethod;
import com.loyalty.common.enums.TransactionCategory;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class TransactionResponse {
    private Long id;
    private String transactionReference;
    private Long customerId;
    private String customerNumber;
    private BigDecimal amount;
    private String currency;
    private String merchantName;
    private String storeLocation;
    private TransactionCategory category;
    private PaymentMethod paymentMethod;
    private String status;
    private Boolean processedForPoints;
    private Long pointsAwarded;
    private String notes;
    private List<TransactionItemDto> items;
    private LocalDateTime transactionDate;
    private LocalDateTime createdAt;
}

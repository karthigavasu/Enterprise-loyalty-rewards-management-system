package com.loyalty.transaction.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class TransactionAnalyticsDto {
    private Long totalTransactions;
    private BigDecimal totalVolume;
    private BigDecimal averageTransactionAmount;
}

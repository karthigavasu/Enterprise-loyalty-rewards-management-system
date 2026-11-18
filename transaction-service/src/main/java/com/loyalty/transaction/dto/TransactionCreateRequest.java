package com.loyalty.transaction.dto;

import com.loyalty.common.enums.PaymentMethod;
import com.loyalty.common.enums.TransactionCategory;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
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
public class TransactionCreateRequest {
    @NotNull(message = "Customer ID is required")
    private Long customerId;

    private String customerNumber;

    @NotNull(message = "Transaction amount is required")
    @DecimalMin(value = "0.01", message = "Transaction amount must be greater than zero")
    private BigDecimal amount;

    private String currency;

    @NotBlank(message = "Merchant name is required")
    private String merchantName;

    private String storeLocation;
    private TransactionCategory category;
    private PaymentMethod paymentMethod;
    private String transactionReference; // If omitted, auto-generated
    private String notes;
    private LocalDateTime transactionDate;
    private List<TransactionItemDto> items;
}

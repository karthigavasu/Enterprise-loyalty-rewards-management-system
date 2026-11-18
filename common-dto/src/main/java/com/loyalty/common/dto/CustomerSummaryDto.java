package com.loyalty.common.dto;

import com.loyalty.common.enums.CustomerStatus;
import com.loyalty.common.enums.TierLevel;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class CustomerSummaryDto {
    private Long customerId;
    private Long userId;
    private String customerNumber;
    private String firstName;
    private String lastName;
    private String email;
    private String phoneNumber;
    private TierLevel tier;
    private CustomerStatus status;
    private LocalDateTime createdAt;
}

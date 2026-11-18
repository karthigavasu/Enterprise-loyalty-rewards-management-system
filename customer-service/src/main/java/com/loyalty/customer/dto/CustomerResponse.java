package com.loyalty.customer.dto;

import com.loyalty.common.enums.CustomerStatus;
import com.loyalty.common.enums.TierLevel;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;
import java.time.LocalDateTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class CustomerResponse {
    private Long id;
    private Long userId;
    private String customerNumber;
    private String email;
    private String firstName;
    private String lastName;
    private String phoneNumber;
    private TierLevel tier;
    private CustomerStatus status;
    private LocalDate dateOfBirth;
    private String address;
    private String city;
    private String state;
    private String postalCode;
    private String country;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}

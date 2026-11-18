package com.loyalty.customer.dto;

import com.loyalty.common.enums.CustomerStatus;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class CustomerStatusUpdateRequest {
    @NotNull(message = "Status is required")
    private CustomerStatus status;
}

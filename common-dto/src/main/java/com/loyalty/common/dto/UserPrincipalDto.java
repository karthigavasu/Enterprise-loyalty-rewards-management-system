package com.loyalty.common.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class UserPrincipalDto {
    private Long userId;
    private String email;
    private String firstName;
    private String lastName;
    private List<String> roles;
    private Long customerId;
    private String customerNumber;
}

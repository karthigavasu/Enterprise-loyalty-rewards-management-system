package com.loyalty.customer.controller;

import com.loyalty.common.dto.ApiResponse;
import com.loyalty.common.enums.CustomerStatus;
import com.loyalty.common.enums.TierLevel;
import com.loyalty.customer.dto.CustomerResponse;
import com.loyalty.customer.dto.CustomerStatusUpdateRequest;
import com.loyalty.customer.dto.CustomerUpdateRequest;
import com.loyalty.customer.service.CustomerService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/customers")
@RequiredArgsConstructor
@Tag(name = "Customer Management", description = "Endpoints for customer profiles, search, status, and updates")
public class CustomerController {

    private final CustomerService customerService;

    @GetMapping
    @PreAuthorize("hasAnyRole('ADMIN', 'STAFF')")
    @Operation(summary = "Search customers with optional filtering and pagination (Admin/Staff)")
    public ResponseEntity<ApiResponse<Page<CustomerResponse>>> searchCustomers(
            @RequestParam(required = false) String query,
            @RequestParam(required = false) TierLevel tier,
            @RequestParam(required = false) CustomerStatus status,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size,
            @RequestParam(defaultValue = "createdAt") String sortBy,
            @RequestParam(defaultValue = "desc") String direction
    ) {
        Sort sort = direction.equalsIgnoreCase("asc") ? Sort.by(sortBy).ascending() : Sort.by(sortBy).descending();
        PageRequest pageRequest = PageRequest.of(page, size, sort);
        Page<CustomerResponse> result = customerService.searchCustomers(query, tier, status, pageRequest);
        return ResponseEntity.ok(ApiResponse.success(result));
    }

    @GetMapping("/{id}")
    @PreAuthorize("hasAnyRole('ADMIN', 'STAFF', 'CUSTOMER')")
    @Operation(summary = "Get customer profile by ID")
    public ResponseEntity<ApiResponse<CustomerResponse>> getCustomerById(@PathVariable Long id) {
        CustomerResponse customer = customerService.getCustomerById(id);
        return ResponseEntity.ok(ApiResponse.success(customer));
    }

    @GetMapping("/by-number/{customerNumber}")
    @PreAuthorize("hasAnyRole('ADMIN', 'STAFF', 'CUSTOMER')")
    @Operation(summary = "Get customer profile by customer number (e.g. CUST-10001)")
    public ResponseEntity<ApiResponse<CustomerResponse>> getCustomerByNumber(@PathVariable String customerNumber) {
        CustomerResponse customer = customerService.getCustomerByCustomerNumber(customerNumber);
        return ResponseEntity.ok(ApiResponse.success(customer));
    }

    @GetMapping("/by-user/{userId}")
    @Operation(summary = "Get customer by User ID")
    public ResponseEntity<ApiResponse<CustomerResponse>> getCustomerByUserId(@PathVariable Long userId) {
        CustomerResponse customer = customerService.getCustomerByUserId(userId);
        return ResponseEntity.ok(ApiResponse.success(customer));
    }

    @GetMapping("/my-profile")
    @PreAuthorize("hasRole('CUSTOMER')")
    @Operation(summary = "Get logged in customer's profile")
    public ResponseEntity<ApiResponse<CustomerResponse>> getMyProfile(Authentication authentication) {
        CustomerResponse customer = customerService.getCustomerByEmail(authentication.getName());
        return ResponseEntity.ok(ApiResponse.success(customer));
    }

    @PutMapping("/{id}")
    @PreAuthorize("hasAnyRole('ADMIN', 'CUSTOMER')")
    @Operation(summary = "Update customer profile information")
    public ResponseEntity<ApiResponse<CustomerResponse>> updateCustomer(
            @PathVariable Long id,
            @Valid @RequestBody CustomerUpdateRequest request
    ) {
        CustomerResponse updated = customerService.updateCustomer(id, request);
        return ResponseEntity.ok(ApiResponse.success("Profile updated successfully", updated));
    }

    @PatchMapping("/{id}/status")
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(summary = "Update customer account status (Admin only)")
    public ResponseEntity<ApiResponse<CustomerResponse>> updateCustomerStatus(
            @PathVariable Long id,
            @Valid @RequestBody CustomerStatusUpdateRequest request
    ) {
        CustomerResponse updated = customerService.updateStatus(id, request.getStatus());
        return ResponseEntity.ok(ApiResponse.success("Customer status updated", updated));
    }

    @PatchMapping("/{id}/tier")
    @PreAuthorize("hasAnyRole('ADMIN', 'STAFF')")
    @Operation(summary = "Update customer loyalty tier")
    public ResponseEntity<ApiResponse<CustomerResponse>> updateCustomerTier(
            @PathVariable Long id,
            @RequestParam TierLevel tier
    ) {
        CustomerResponse updated = customerService.updateTier(id, tier);
        return ResponseEntity.ok(ApiResponse.success("Customer tier updated", updated));
    }
}

package com.loyalty.transaction.controller;

import com.loyalty.common.dto.ApiResponse;
import com.loyalty.common.enums.TransactionCategory;
import com.loyalty.common.security.JwtTokenProvider;
import com.loyalty.transaction.dto.TransactionAnalyticsDto;
import com.loyalty.transaction.dto.TransactionCreateRequest;
import com.loyalty.transaction.dto.TransactionResponse;
import com.loyalty.transaction.service.CustomerTransactionService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;

@RestController
@RequestMapping("/api/transactions")
@RequiredArgsConstructor
@Tag(name = "Transaction Management", description = "Endpoints for registering, tracking, and auditing customer transactions")
public class TransactionController {

    private final CustomerTransactionService transactionService;
    private final JwtTokenProvider jwtTokenProvider;

    @PostMapping
    @PreAuthorize("hasAnyRole('STAFF', 'ADMIN')")
    @Operation(summary = "Register customer purchase transaction (Staff/Admin)")
    public ResponseEntity<ApiResponse<TransactionResponse>> registerTransaction(
            @Valid @RequestBody TransactionCreateRequest request
    ) {
        TransactionResponse response = transactionService.createTransaction(request);
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.success("Transaction registered and loyalty points processing initiated", response));
    }

    @GetMapping
    @PreAuthorize("hasAnyRole('ADMIN', 'STAFF')")
    @Operation(summary = "Search and filter customer transactions (Admin/Staff)")
    public ResponseEntity<ApiResponse<Page<TransactionResponse>>> getTransactions(
            @RequestParam(required = false) Long customerId,
            @RequestParam(required = false) String merchant,
            @RequestParam(required = false) TransactionCategory category,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime startDate,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime endDate,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size,
            @RequestParam(defaultValue = "transactionDate") String sortBy,
            @RequestParam(defaultValue = "desc") String direction
    ) {
        Sort sort = direction.equalsIgnoreCase("asc") ? Sort.by(sortBy).ascending() : Sort.by(sortBy).descending();
        Page<TransactionResponse> result = transactionService.searchTransactions(customerId, merchant, category, startDate, endDate, PageRequest.of(page, size, sort));
        return ResponseEntity.ok(ApiResponse.success(result));
    }

    @GetMapping("/my-transactions")
    @PreAuthorize("hasRole('CUSTOMER')")
    @Operation(summary = "Get current logged-in customer's transaction history")
    public ResponseEntity<ApiResponse<Page<TransactionResponse>>> getMyTransactions(
            @RequestHeader(value = "Authorization", required = false) String authHeader,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size
    ) {
        Long customerId = extractCustomerIdFromToken(authHeader);
        if (customerId == null) {
            return ResponseEntity.badRequest().body(ApiResponse.error("Unable to identify customer account"));
        }
        Page<TransactionResponse> result = transactionService.getCustomerTransactions(customerId, PageRequest.of(page, size));
        return ResponseEntity.ok(ApiResponse.success(result));
    }

    @GetMapping("/{id}")
    @PreAuthorize("hasAnyRole('ADMIN', 'STAFF', 'CUSTOMER')")
    @Operation(summary = "Get transaction by ID")
    public ResponseEntity<ApiResponse<TransactionResponse>> getById(@PathVariable Long id) {
        TransactionResponse response = transactionService.getTransactionById(id);
        return ResponseEntity.ok(ApiResponse.success(response));
    }

    @GetMapping("/reference/{reference}")
    @PreAuthorize("hasAnyRole('ADMIN', 'STAFF', 'CUSTOMER')")
    @Operation(summary = "Lookup transaction by reference number")
    public ResponseEntity<ApiResponse<TransactionResponse>> getByReference(@PathVariable String reference) {
        TransactionResponse response = transactionService.getByReference(reference);
        return ResponseEntity.ok(ApiResponse.success(response));
    }

    @GetMapping("/analytics/summary")
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(summary = "Get aggregate transaction volume and average spend (Admin only)")
    public ResponseEntity<ApiResponse<TransactionAnalyticsDto>> getAnalytics() {
        TransactionAnalyticsDto analytics = transactionService.getAnalytics();
        return ResponseEntity.ok(ApiResponse.success(analytics));
    }

    private Long extractCustomerIdFromToken(String authHeader) {
        if (authHeader != null && authHeader.startsWith("Bearer ")) {
            return jwtTokenProvider.getCustomerIdFromToken(authHeader.substring(7));
        }
        return null;
    }
}

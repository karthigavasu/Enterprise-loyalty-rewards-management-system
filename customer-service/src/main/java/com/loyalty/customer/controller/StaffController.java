package com.loyalty.customer.controller;

import com.loyalty.common.dto.ApiResponse;
import com.loyalty.customer.dto.StaffCreateRequest;
import com.loyalty.customer.dto.StaffResponse;
import com.loyalty.customer.service.StaffService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/staff")
@RequiredArgsConstructor
@PreAuthorize("hasRole('ADMIN')")
@Tag(name = "Staff Management", description = "Endpoints for managing staff members (Admin only)")
public class StaffController {

    private final StaffService staffService;

    @GetMapping
    @Operation(summary = "Get list of all staff members (Admin only)")
    public ResponseEntity<ApiResponse<Page<StaffResponse>>> getAllStaff(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size
    ) {
        PageRequest pageRequest = PageRequest.of(page, size, Sort.by("createdAt").descending());
        Page<StaffResponse> staff = staffService.getAllStaff(pageRequest);
        return ResponseEntity.ok(ApiResponse.success(staff));
    }

    @GetMapping("/{id}")
    @Operation(summary = "Get staff member by ID (Admin only)")
    public ResponseEntity<ApiResponse<StaffResponse>> getStaffById(@PathVariable Long id) {
        StaffResponse staff = staffService.getStaffById(id);
        return ResponseEntity.ok(ApiResponse.success(staff));
    }

    @PostMapping
    @Operation(summary = "Register a new staff account (Admin only)")
    public ResponseEntity<ApiResponse<StaffResponse>> createStaff(@Valid @RequestBody StaffCreateRequest request) {
        StaffResponse created = staffService.createStaff(request);
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.success("Staff account created successfully", created));
    }

    @PatchMapping("/{id}/status")
    @Operation(summary = "Toggle staff member active status (Admin only)")
    public ResponseEntity<ApiResponse<StaffResponse>> toggleStaffStatus(
            @PathVariable Long id,
            @RequestParam boolean active
    ) {
        StaffResponse updated = staffService.toggleStaffStatus(id, active);
        return ResponseEntity.ok(ApiResponse.success("Staff status updated", updated));
    }
}

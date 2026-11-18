package com.loyalty.customer.service;

import com.loyalty.common.constants.SecurityConstants;
import com.loyalty.customer.dto.StaffCreateRequest;
import com.loyalty.customer.dto.StaffResponse;
import com.loyalty.customer.entity.Role;
import com.loyalty.customer.entity.User;
import com.loyalty.customer.exception.BadRequestException;
import com.loyalty.customer.exception.ResourceNotFoundException;
import com.loyalty.customer.repository.RoleRepository;
import com.loyalty.customer.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.HashSet;
import java.util.Set;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
public class StaffService {

    private final UserRepository userRepository;
    private final RoleRepository roleRepository;
    private final PasswordEncoder passwordEncoder;

    @Transactional(readOnly = true)
    public Page<StaffResponse> getAllStaff(Pageable pageable) {
        return userRepository.findAllByRoleName(SecurityConstants.ROLE_STAFF, pageable)
                .map(this::mapToStaffResponse);
    }

    @Transactional(readOnly = true)
    public StaffResponse getStaffById(Long id) {
        User user = userRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Staff user not found with id: " + id));
        return mapToStaffResponse(user);
    }

    @Transactional
    public StaffResponse createStaff(StaffCreateRequest request) {
        String email = request.getEmail().toLowerCase().trim();
        if (userRepository.existsByEmail(email)) {
            throw new BadRequestException("An account with email " + email + " already exists");
        }

        Role staffRole = roleRepository.findByName(SecurityConstants.ROLE_STAFF)
                .orElseGet(() -> roleRepository.save(Role.builder()
                        .name(SecurityConstants.ROLE_STAFF)
                        .description("Staff Role")
                        .build()));

        Set<Role> roles = new HashSet<>();
        roles.add(staffRole);

        User user = User.builder()
                .email(email)
                .password(passwordEncoder.encode(request.getPassword()))
                .firstName(request.getFirstName())
                .lastName(request.getLastName())
                .phoneNumber(request.getPhoneNumber())
                .active(true)
                .roles(roles)
                .build();

        user = userRepository.save(user);
        log.info("Created new staff member: {}", email);
        return mapToStaffResponse(user);
    }

    @Transactional
    public StaffResponse toggleStaffStatus(Long id, boolean active) {
        User user = userRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Staff user not found with id: " + id));

        user.setActive(active);
        user = userRepository.save(user);
        log.info("Updated staff status {} for id {}", active, id);
        return mapToStaffResponse(user);
    }

    private StaffResponse mapToStaffResponse(User user) {
        return StaffResponse.builder()
                .id(user.getId())
                .email(user.getEmail())
                .firstName(user.getFirstName())
                .lastName(user.getLastName())
                .phoneNumber(user.getPhoneNumber())
                .active(user.isActive())
                .roles(user.getRoles().stream().map(Role::getName).collect(Collectors.toList()))
                .createdAt(user.getCreatedAt())
                .build();
    }
}

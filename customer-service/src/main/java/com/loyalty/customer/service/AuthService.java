package com.loyalty.customer.service;

import com.loyalty.common.constants.SecurityConstants;
import com.loyalty.common.enums.CustomerStatus;
import com.loyalty.common.enums.TierLevel;
import com.loyalty.common.security.JwtTokenProvider;
import com.loyalty.customer.dto.AuthRequest;
import com.loyalty.customer.dto.AuthResponse;
import com.loyalty.customer.dto.RegisterRequest;
import com.loyalty.customer.entity.Customer;
import com.loyalty.customer.entity.Role;
import com.loyalty.customer.entity.User;
import com.loyalty.customer.exception.BadRequestException;
import com.loyalty.customer.exception.ResourceNotFoundException;
import com.loyalty.customer.exception.UnauthorizedException;
import com.loyalty.customer.repository.CustomerRepository;
import com.loyalty.customer.repository.RoleRepository;
import com.loyalty.customer.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.concurrent.ThreadLocalRandom;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
public class AuthService {

    private final UserRepository userRepository;
    private final RoleRepository roleRepository;
    private final CustomerRepository customerRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtTokenProvider jwtTokenProvider;

    @Transactional
    public AuthResponse login(AuthRequest request) {
        User user = userRepository.findByEmail(request.getEmail().toLowerCase().trim())
                .orElseThrow(() -> new UnauthorizedException("Invalid email or password"));

        if (!user.isActive()) {
            throw new UnauthorizedException("User account is deactivated");
        }

        if (!passwordEncoder.matches(request.getPassword(), user.getPassword())) {
            throw new UnauthorizedException("Invalid email or password");
        }

        List<String> roles = user.getRoles().stream()
                .map(Role::getName)
                .collect(Collectors.toList());

        Long customerId = null;
        String customerNumber = null;
        String tier = null;

        var customerOpt = customerRepository.findByUserId(user.getId());
        if (customerOpt.isPresent()) {
            Customer customer = customerOpt.get();
            customerId = customer.getId();
            customerNumber = customer.getCustomerNumber();
            tier = customer.getTier().name();
        }

        String token = jwtTokenProvider.generateToken(
                user.getEmail(),
                user.getId(),
                roles,
                customerId,
                customerNumber
        );

        log.info("User logged in successfully: {}", user.getEmail());

        return AuthResponse.builder()
                .token(token)
                .type("Bearer")
                .userId(user.getId())
                .email(user.getEmail())
                .firstName(user.getFirstName())
                .lastName(user.getLastName())
                .roles(roles)
                .customerId(customerId)
                .customerNumber(customerNumber)
                .tier(tier)
                .build();
    }

    @Transactional
    public AuthResponse register(RegisterRequest request) {
        String email = request.getEmail().toLowerCase().trim();
        if (userRepository.existsByEmail(email)) {
            throw new BadRequestException("An account with email " + email + " already exists");
        }

        Role customerRole = roleRepository.findByName(SecurityConstants.ROLE_CUSTOMER)
                .orElseGet(() -> roleRepository.save(Role.builder()
                        .name(SecurityConstants.ROLE_CUSTOMER)
                        .description("Customer Role")
                        .build()));

        Set<Role> roles = new HashSet<>();
        roles.add(customerRole);

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

        // Generate unique customer number
        String customerNumber = generateCustomerNumber();

        Customer customer = Customer.builder()
                .user(user)
                .customerNumber(customerNumber)
                .tier(TierLevel.BRONZE)
                .status(CustomerStatus.ACTIVE)
                .dateOfBirth(request.getDateOfBirth())
                .address(request.getAddress())
                .city(request.getCity())
                .state(request.getState())
                .postalCode(request.getPostalCode())
                .country(request.getCountry())
                .build();

        customer = customerRepository.save(customer);

        List<String> roleNames = List.of(customerRole.getName());
        String token = jwtTokenProvider.generateToken(
                user.getEmail(),
                user.getId(),
                roleNames,
                customer.getId(),
                customer.getCustomerNumber()
        );

        log.info("Registered new customer with number: {} and email: {}", customerNumber, email);

        return AuthResponse.builder()
                .token(token)
                .type("Bearer")
                .userId(user.getId())
                .email(user.getEmail())
                .firstName(user.getFirstName())
                .lastName(user.getLastName())
                .roles(roleNames)
                .customerId(customer.getId())
                .customerNumber(customer.getCustomerNumber())
                .tier(customer.getTier().name())
                .build();
    }

    @Transactional(readOnly = true)
    public AuthResponse getMe(String email) {
        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new ResourceNotFoundException("User not found with email: " + email));

        List<String> roles = user.getRoles().stream()
                .map(Role::getName)
                .collect(Collectors.toList());

        Long customerId = null;
        String customerNumber = null;
        String tier = null;

        var customerOpt = customerRepository.findByUserId(user.getId());
        if (customerOpt.isPresent()) {
            Customer customer = customerOpt.get();
            customerId = customer.getId();
            customerNumber = customer.getCustomerNumber();
            tier = customer.getTier().name();
        }

        return AuthResponse.builder()
                .userId(user.getId())
                .email(user.getEmail())
                .firstName(user.getFirstName())
                .lastName(user.getLastName())
                .roles(roles)
                .customerId(customerId)
                .customerNumber(customerNumber)
                .tier(tier)
                .build();
    }

    private String generateCustomerNumber() {
        int randomNum = ThreadLocalRandom.current().nextInt(100000, 999999);
        return "CUST-" + randomNum;
    }
}

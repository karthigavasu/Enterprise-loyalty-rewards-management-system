package com.loyalty.customer.service;

import com.loyalty.common.enums.CustomerStatus;
import com.loyalty.common.enums.TierLevel;
import com.loyalty.customer.dto.CustomerResponse;
import com.loyalty.customer.dto.CustomerUpdateRequest;
import com.loyalty.customer.entity.Customer;
import com.loyalty.customer.entity.User;
import com.loyalty.customer.exception.ResourceNotFoundException;
import com.loyalty.customer.repository.CustomerRepository;
import com.loyalty.customer.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Slf4j
public class CustomerService {

    private final CustomerRepository customerRepository;
    private final UserRepository userRepository;

    @Transactional(readOnly = true)
    public Page<CustomerResponse> searchCustomers(String query, TierLevel tier, CustomerStatus status, Pageable pageable) {
        return customerRepository.searchCustomers(query, tier, status, pageable)
                .map(this::mapToResponse);
    }

    @Transactional(readOnly = true)
    public CustomerResponse getCustomerById(Long id) {
        Customer customer = customerRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Customer not found with id: " + id));
        return mapToResponse(customer);
    }

    @Transactional(readOnly = true)
    public CustomerResponse getCustomerByCustomerNumber(String customerNumber) {
        Customer customer = customerRepository.findByCustomerNumber(customerNumber)
                .orElseThrow(() -> new ResourceNotFoundException("Customer not found with number: " + customerNumber));
        return mapToResponse(customer);
    }

    @Transactional(readOnly = true)
    public CustomerResponse getCustomerByUserId(Long userId) {
        Customer customer = customerRepository.findByUserId(userId)
                .orElseThrow(() -> new ResourceNotFoundException("Customer not found for user id: " + userId));
        return mapToResponse(customer);
    }

    @Transactional(readOnly = true)
    public CustomerResponse getCustomerByEmail(String email) {
        Customer customer = customerRepository.findByUserEmail(email)
                .orElseThrow(() -> new ResourceNotFoundException("Customer not found for email: " + email));
        return mapToResponse(customer);
    }

    @Transactional
    public CustomerResponse updateCustomer(Long id, CustomerUpdateRequest request) {
        Customer customer = customerRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Customer not found with id: " + id));

        User user = customer.getUser();
        user.setFirstName(request.getFirstName());
        user.setLastName(request.getLastName());
        if (request.getPhoneNumber() != null) {
            user.setPhoneNumber(request.getPhoneNumber());
        }
        userRepository.save(user);

        if (request.getDateOfBirth() != null) {
            customer.setDateOfBirth(request.getDateOfBirth());
        }
        if (request.getAddress() != null) {
            customer.setAddress(request.getAddress());
        }
        if (request.getCity() != null) {
            customer.setCity(request.getCity());
        }
        if (request.getState() != null) {
            customer.setState(request.getState());
        }
        if (request.getPostalCode() != null) {
            customer.setPostalCode(request.getPostalCode());
        }
        if (request.getCountry() != null) {
            customer.setCountry(request.getCountry());
        }

        Customer saved = customerRepository.save(customer);
        log.info("Updated customer profile for id: {}", id);
        return mapToResponse(saved);
    }

    @Transactional
    public CustomerResponse updateStatus(Long id, CustomerStatus status) {
        Customer customer = customerRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Customer not found with id: " + id));

        customer.setStatus(status);
        if (status == CustomerStatus.SUSPENDED || status == CustomerStatus.INACTIVE) {
            customer.getUser().setActive(false);
        } else if (status == CustomerStatus.ACTIVE) {
            customer.getUser().setActive(true);
        }
        userRepository.save(customer.getUser());
        Customer saved = customerRepository.save(customer);
        log.info("Updated customer {} status to {}", id, status);
        return mapToResponse(saved);
    }

    @Transactional
    public CustomerResponse updateTier(Long id, TierLevel tier) {
        Customer customer = customerRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Customer not found with id: " + id));

        customer.setTier(tier);
        Customer saved = customerRepository.save(customer);
        log.info("Updated customer {} tier to {}", id, tier);
        return mapToResponse(saved);
    }

    public CustomerResponse mapToResponse(Customer customer) {
        User user = customer.getUser();
        return CustomerResponse.builder()
                .id(customer.getId())
                .userId(user.getId())
                .customerNumber(customer.getCustomerNumber())
                .email(user.getEmail())
                .firstName(user.getFirstName())
                .lastName(user.getLastName())
                .phoneNumber(user.getPhoneNumber())
                .tier(customer.getTier())
                .status(customer.getStatus())
                .dateOfBirth(customer.getDateOfBirth())
                .address(customer.getAddress())
                .city(customer.getCity())
                .state(customer.getState())
                .postalCode(customer.getPostalCode())
                .country(customer.getCountry())
                .createdAt(customer.getCreatedAt())
                .updatedAt(customer.getUpdatedAt())
                .build();
    }
}

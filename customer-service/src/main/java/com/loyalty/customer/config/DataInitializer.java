package com.loyalty.customer.config;

import com.loyalty.common.constants.SecurityConstants;
import com.loyalty.common.enums.CustomerStatus;
import com.loyalty.common.enums.TierLevel;
import com.loyalty.customer.entity.Customer;
import com.loyalty.customer.entity.Role;
import com.loyalty.customer.entity.User;
import com.loyalty.customer.repository.CustomerRepository;
import com.loyalty.customer.repository.RoleRepository;
import com.loyalty.customer.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.CommandLineRunner;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.HashSet;
import java.util.Set;

@Component
@RequiredArgsConstructor
@Slf4j
public class DataInitializer implements CommandLineRunner {

    private final UserRepository userRepository;
    private final RoleRepository roleRepository;
    private final CustomerRepository customerRepository;
    private final PasswordEncoder passwordEncoder;

    @Override
    @Transactional
    public void run(String... args) {
        log.info("Initializing system roles and seed users...");

        Role adminRole = getOrCreateRole(SecurityConstants.ROLE_ADMIN, "Platform Administrator");
        Role staffRole = getOrCreateRole(SecurityConstants.ROLE_STAFF, "Support and Operations Staff");
        Role customerRole = getOrCreateRole(SecurityConstants.ROLE_CUSTOMER, "Registered Customer");

        // Seed Admin user
        if (!userRepository.existsByEmail("admin@loyalty.com")) {
            User admin = User.builder()
                    .email("admin@loyalty.com")
                    .password(passwordEncoder.encode("Admin@123"))
                    .firstName("System")
                    .lastName("Administrator")
                    .phoneNumber("+1-555-0100")
                    .active(true)
                    .roles(Set.of(adminRole))
                    .build();
            userRepository.save(admin);
            log.info("Created default Admin user: admin@loyalty.com / Admin@123");
        }

        // Seed Staff user
        if (!userRepository.existsByEmail("staff@loyalty.com")) {
            User staff = User.builder()
                    .email("staff@loyalty.com")
                    .password(passwordEncoder.encode("Staff@123"))
                    .firstName("Operations")
                    .lastName("Staff")
                    .phoneNumber("+1-555-0101")
                    .active(true)
                    .roles(Set.of(staffRole))
                    .build();
            userRepository.save(staff);
            log.info("Created default Staff user: staff@loyalty.com / Staff@123");
        }

        // Seed Demo Customers
        createDemoCustomer("john.doe@example.com", "John", "Doe", "+1-555-0111", "CUST-10001", TierLevel.GOLD, customerRole, "123 Market St", "San Francisco", "CA");
        createDemoCustomer("sarah.connor@example.com", "Sarah", "Connor", "+1-555-0112", "CUST-10002", TierLevel.PLATINUM, customerRole, "456 Tech Blvd", "Austin", "TX");
        createDemoCustomer("alex.morgan@example.com", "Alex", "Morgan", "+1-555-0113", "CUST-10003", TierLevel.SILVER, customerRole, "789 Pine Ave", "Seattle", "WA");
        createDemoCustomer("emily.watson@example.com", "Emily", "Watson", "+1-555-0114", "CUST-10004", TierLevel.BRONZE, customerRole, "101 Maple Rd", "Denver", "CO");

        log.info("Data initialization completed.");
    }

    private void createDemoCustomer(String email, String firstName, String lastName, String phone, String customerNumber, TierLevel tier, Role customerRole, String address, String city, String state) {
        if (!userRepository.existsByEmail(email)) {
            User user = User.builder()
                    .email(email)
                    .password(passwordEncoder.encode("Customer@123"))
                    .firstName(firstName)
                    .lastName(lastName)
                    .phoneNumber(phone)
                    .active(true)
                    .roles(new HashSet<>(Set.of(customerRole)))
                    .build();
            user = userRepository.save(user);

            Customer customer = Customer.builder()
                    .user(user)
                    .customerNumber(customerNumber)
                    .tier(tier)
                    .status(CustomerStatus.ACTIVE)
                    .dateOfBirth(LocalDate.of(1990, 5, 15))
                    .address(address)
                    .city(city)
                    .state(state)
                    .postalCode("94105")
                    .country("USA")
                    .build();
            customerRepository.save(customer);
            log.info("Created demo customer: {} ({}) with tier {}", email, customerNumber, tier);
        }
    }

    private Role getOrCreateRole(String roleName, String description) {
        return roleRepository.findByName(roleName)
                .orElseGet(() -> roleRepository.save(Role.builder()
                        .name(roleName)
                        .description(description)
                        .build()));
    }
}

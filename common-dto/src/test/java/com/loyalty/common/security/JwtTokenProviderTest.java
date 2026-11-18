package com.loyalty.common.security;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

class JwtTokenProviderTest {

    private JwtTokenProvider jwtTokenProvider;

    @BeforeEach
    void setUp() {
        jwtTokenProvider = new JwtTokenProvider();
    }

    @Test
    void testGenerateAndValidateToken() {
        String token = jwtTokenProvider.generateToken("john@example.com", 1L, List.of("ROLE_CUSTOMER"), 101L, "CUST-1001");
        assertThat(token).isNotBlank();
        assertThat(jwtTokenProvider.validateToken(token)).isTrue();
        assertThat(jwtTokenProvider.getUsernameFromToken(token)).isEqualTo("john@example.com");
        assertThat(jwtTokenProvider.getUserIdFromToken(token)).isEqualTo(1L);
        assertThat(jwtTokenProvider.getCustomerIdFromToken(token)).isEqualTo(101L);
        assertThat(jwtTokenProvider.getCustomerNumberFromToken(token)).isEqualTo("CUST-1001");
        assertThat(jwtTokenProvider.getRolesFromToken(token)).contains("ROLE_CUSTOMER");
    }

    @Test
    void testInvalidToken() {
        assertThat(jwtTokenProvider.validateToken("invalid.token.here")).isFalse();
    }
}

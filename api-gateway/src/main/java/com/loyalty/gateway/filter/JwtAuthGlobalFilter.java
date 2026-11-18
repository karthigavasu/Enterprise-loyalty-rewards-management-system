package com.loyalty.gateway.filter;

import com.loyalty.common.constants.SecurityConstants;
import com.loyalty.common.security.JwtTokenProvider;
import lombok.extern.slf4j.Slf4j;
import org.springframework.cloud.gateway.filter.GatewayFilterChain;
import org.springframework.cloud.gateway.filter.GlobalFilter;
import org.springframework.core.Ordered;
import org.springframework.http.HttpHeaders;
import org.springframework.http.server.reactive.ServerHttpRequest;
import org.springframework.stereotype.Component;
import org.springframework.web.server.ServerWebExchange;
import reactor.core.publisher.Mono;

import java.util.List;

@Component
@Slf4j
public class JwtAuthGlobalFilter implements GlobalFilter, Ordered {

    private final JwtTokenProvider jwtTokenProvider = new JwtTokenProvider();

    @Override
    public Mono<Void> filter(ServerWebExchange exchange, GatewayFilterChain chain) {
        ServerHttpRequest request = exchange.getRequest();
        HttpHeaders headers = request.getHeaders();
        String authHeader = headers.getFirst(SecurityConstants.TOKEN_HEADER);

        if (authHeader != null && authHeader.startsWith(SecurityConstants.TOKEN_PREFIX)) {
            String token = authHeader.substring(SecurityConstants.TOKEN_PREFIX.length());
            try {
                if (jwtTokenProvider.validateToken(token)) {
                    String username = jwtTokenProvider.getUsernameFromToken(token);
                    Long userId = jwtTokenProvider.getUserIdFromToken(token);
                    List<String> roles = jwtTokenProvider.getRolesFromToken(token);
                    Long customerId = jwtTokenProvider.getCustomerIdFromToken(token);
                    String customerNumber = jwtTokenProvider.getCustomerNumberFromToken(token);

                    ServerHttpRequest.Builder mutatedRequestBuilder = request.mutate()
                            .header(SecurityConstants.HEADER_USER_EMAIL, username);

                    if (userId != null) {
                        mutatedRequestBuilder.header(SecurityConstants.HEADER_USER_ID, userId.toString());
                    }
                    if (roles != null && !roles.isEmpty()) {
                        mutatedRequestBuilder.header(SecurityConstants.HEADER_USER_ROLES, String.join(",", roles));
                    }
                    if (customerId != null) {
                        mutatedRequestBuilder.header(SecurityConstants.HEADER_CUSTOMER_ID, customerId.toString());
                    }
                    if (customerNumber != null) {
                        mutatedRequestBuilder.header(SecurityConstants.HEADER_CUSTOMER_NUMBER, customerNumber);
                    }

                    return chain.filter(exchange.mutate().request(mutatedRequestBuilder.build()).build());
                }
            } catch (Exception ex) {
                log.warn("Invalid JWT in API Gateway: {}", ex.getMessage());
            }
        }

        return chain.filter(exchange);
    }

    @Override
    public int getOrder() {
        return -100; // Run early in the gateway filter chain
    }
}

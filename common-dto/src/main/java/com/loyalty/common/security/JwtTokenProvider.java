package com.loyalty.common.security;

import com.loyalty.common.constants.SecurityConstants;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.ExpiredJwtException;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.MalformedJwtException;
import io.jsonwebtoken.UnsupportedJwtException;
import io.jsonwebtoken.security.Keys;
import io.jsonwebtoken.security.SignatureException;

import javax.crypto.SecretKey;
import java.nio.charset.StandardCharsets;
import java.util.Date;
import java.util.List;

public class JwtTokenProvider {

    private final SecretKey key;
    private final long expirationMs;

    public JwtTokenProvider() {
        this(SecurityConstants.DEFAULT_JWT_SECRET, SecurityConstants.JWT_EXPIRATION_MS);
    }

    public JwtTokenProvider(String secret, long expirationMs) {
        String effectiveSecret = (secret != null && secret.length() >= 32) ? secret : SecurityConstants.DEFAULT_JWT_SECRET;
        this.key = Keys.hmacShaKeyFor(effectiveSecret.getBytes(StandardCharsets.UTF_8));
        this.expirationMs = expirationMs > 0 ? expirationMs : SecurityConstants.JWT_EXPIRATION_MS;
    }

    public String generateToken(String username, Long userId, List<String> roles, Long customerId, String customerNumber) {
        Date now = new Date();
        Date expiryDate = new Date(now.getTime() + expirationMs);

        var builder = Jwts.builder()
                .subject(username)
                .claim("userId", userId)
                .claim("roles", roles)
                .issuedAt(now)
                .expiration(expiryDate)
                .signWith(key);

        if (customerId != null) {
            builder.claim("customerId", customerId);
        }
        if (customerNumber != null) {
            builder.claim("customerNumber", customerNumber);
        }

        return builder.compact();
    }

    public Claims getClaimsFromToken(String token) {
        return Jwts.parser()
                .verifyWith(key)
                .build()
                .parseSignedClaims(token)
                .getPayload();
    }

    public String getUsernameFromToken(String token) {
        return getClaimsFromToken(token).getSubject();
    }

    public Long getUserIdFromToken(String token) {
        Object val = getClaimsFromToken(token).get("userId");
        if (val instanceof Number num) {
            return num.longValue();
        }
        return null;
    }

    @SuppressWarnings("unchecked")
    public List<String> getRolesFromToken(String token) {
        Object roles = getClaimsFromToken(token).get("roles");
        if (roles instanceof List<?> list) {
            return (List<String>) list;
        }
        return List.of();
    }

    public Long getCustomerIdFromToken(String token) {
        Object val = getClaimsFromToken(token).get("customerId");
        if (val instanceof Number num) {
            return num.longValue();
        }
        return null;
    }

    public String getCustomerNumberFromToken(String token) {
        Object val = getClaimsFromToken(token).get("customerNumber");
        return val != null ? val.toString() : null;
    }

    public boolean validateToken(String token) {
        try {
            Jwts.parser().verifyWith(key).build().parseSignedClaims(token);
            return true;
        } catch (SignatureException | MalformedJwtException | ExpiredJwtException | UnsupportedJwtException | IllegalArgumentException e) {
            return false;
        }
    }
}

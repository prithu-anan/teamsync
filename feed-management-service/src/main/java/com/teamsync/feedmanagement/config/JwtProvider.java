package com.teamsync.feedmanagement.config;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;
import org.springframework.stereotype.Component;

import javax.crypto.SecretKey;
import java.util.Date;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@Component
public class JwtProvider {

    private static final Logger log = LoggerFactory.getLogger(JwtProvider.class);

    private final JwtConstant jwtConstant;
    private final SecretKey key;

    public JwtProvider(JwtConstant jwtConstant) {
        this.jwtConstant = jwtConstant;
        this.key = Keys.hmacShaKeyFor(jwtConstant.getSecretKey().getBytes());
    }

    public boolean validateToken(String token) {
        try {
            if (token.startsWith("Bearer ")) {
                token = token.substring(7);
            }
            log.debug("Validating JWT token, length: {}", token.length());
            Jwts.parser()
                .verifyWith(key)
                .build()
                .parseSignedClaims(token);
            log.debug("JWT token validation successful");
            return true;
        } catch (Exception e) {
            log.warn("JWT token validation failed: {}", e.getMessage());
            return false;
        }
    }

    public String getEmailFromToken(String token) {
        try {
            if (token.startsWith("Bearer ")) {
                token = token.substring(7);
            }
            Claims claims = Jwts.parser()
                .verifyWith(key)
                .build()
                .parseSignedClaims(token)
                .getPayload();
            return claims.get("email", String.class);
        } catch (Exception e) {
            return null;
        }
    }

    public String getSubjectFromToken(String token) {
        try {
            if (token.startsWith("Bearer ")) {
                token = token.substring(7);
            }
            log.debug("Extracting subject from JWT token");
            Claims claims = Jwts.parser()
                .verifyWith(key)
                .build()
                .parseSignedClaims(token)
                .getPayload();
            String subject = claims.getSubject();
            log.debug("Subject extracted from token: {}", subject);
            return subject;
        } catch (Exception e) {
            log.warn("Failed to extract subject from JWT token: {}", e.getMessage());
            return null;
        }
    }

    public Date getExpirationDateFromToken(String token) {
        try {
            if (token.startsWith("Bearer ")) {
                token = token.substring(7);
            }
            Claims claims = Jwts.parser()
                .verifyWith(key)
                .build()
                .parseSignedClaims(token)
                .getPayload();
            return claims.getExpiration();
        } catch (Exception e) {
            return null;
        }
    }

    public boolean isTokenExpired(String token) {
        Date expiration = getExpirationDateFromToken(token);
        boolean expired = expiration != null && expiration.before(new Date());
        log.debug("Token expiration check - expiration: {}, expired: {}", expiration, expired);
        return expired;
    }

    public String generateServiceToken() {
        log.debug("Generating service token for feed-management-service");
        log.debug("Secret key length: {}", key.getEncoded().length);
        log.debug("Expiration time: {} ms", jwtConstant.getExpirationTime());
        
        String token = Jwts.builder()
                .setSubject("feed-management-service")
                .setIssuedAt(new Date(System.currentTimeMillis()))
                .setExpiration(new Date(System.currentTimeMillis() + jwtConstant.getExpirationTime()))
                .signWith(key)
                .compact();
        
        log.debug("Service token generated successfully, length: {}", token.length());
        log.debug("Generated token starts with: {}", token.substring(0, Math.min(20, token.length())));
        
        // Validate the generated token
        log.debug("Validating generated token: {}", validateToken(token));
        log.debug("Generated token subject: {}", getSubjectFromToken(token));
        log.debug("Generated token expired: {}", isTokenExpired(token));
        
        return token;
    }
}

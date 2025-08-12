package com.teamsync.auth.service;

import com.teamsync.auth.entity.BlacklistedToken;
import com.teamsync.auth.repository.BlacklistedTokenRepository;
import com.teamsync.auth.config.JwtProvider;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.util.Date;

@Slf4j
@Service
@RequiredArgsConstructor
public class TokenBlacklistService {

    private final BlacklistedTokenRepository blacklistedTokenRepository;
    private final JwtProvider jwtProvider;

    @Transactional
    public void blacklistToken(String token) {
        if (token == null || token.trim().isEmpty()) {
            return;
        }

        // Check if token is already blacklisted
        if (blacklistedTokenRepository.existsByToken(token)) {
            return;
        }

        try {
            // Get expiration date from token
            Date expirationDate = jwtProvider.getExpirationDateFromToken(token);
            Instant expiresAt = expirationDate.toInstant();

            // Create blacklisted token entity
            BlacklistedToken blacklistedToken = BlacklistedToken.builder()
                    .token(token)
                    .blacklistedAt(Instant.now())
                    .expiresAt(expiresAt)
                    .build();

            // Save to database
            blacklistedTokenRepository.save(blacklistedToken);
            log.info("Token blacklisted successfully");
        } catch (Exception e) {
            log.error("Error blacklisting token: {}", e.getMessage());
        }
    }

    public boolean isTokenBlacklisted(String token) {
        if (token == null || token.trim().isEmpty()) {
            return false;
        }
        return blacklistedTokenRepository.existsByToken(token);
    }

    @Transactional
    public void cleanupExpiredTokens() {
        // This method can be called periodically to clean up expired blacklisted tokens
        // Implementation depends on your specific requirements
        log.info("Cleaning up expired blacklisted tokens");
    }
}

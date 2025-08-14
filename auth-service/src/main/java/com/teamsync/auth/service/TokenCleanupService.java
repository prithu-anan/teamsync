package com.teamsync.auth.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

@Slf4j
@Service
@RequiredArgsConstructor
public class TokenCleanupService {

    private final TokenBlacklistService tokenBlacklistService;

    // Clean up expired tokens every hour
    @Scheduled(fixedRate = 3600000) // 1 hour in milliseconds
    public void cleanupExpiredTokens() {
        log.info("Starting scheduled cleanup of expired blacklisted tokens");
        tokenBlacklistService.cleanupExpiredTokens();
    }
}

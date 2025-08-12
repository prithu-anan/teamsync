package com.teamsync.auth.service;

import com.teamsync.auth.dto.authDTO.TokenRefreshResponseDTO;
import com.teamsync.auth.entity.RefreshToken;
import com.teamsync.auth.entity.Users;
import com.teamsync.auth.repository.RefreshTokenRepository;
import com.teamsync.auth.config.JwtProvider;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.util.Optional;
import java.util.UUID;

@Slf4j
@Service
@RequiredArgsConstructor
public class RefreshTokenService {

    private final RefreshTokenRepository refreshTokenRepository;
    private final JwtProvider jwtProvider;

    @Transactional
    public String createRefreshToken(Users user, HttpServletRequest request) {
        // Generate unique refresh token
        String refreshToken = UUID.randomUUID().toString();

        // Get client information
        String userAgent = request.getHeader("User-Agent");
        String ipAddress = getClientIpAddress(request);

        // Create refresh token entity
        RefreshToken tokenEntity = RefreshToken.builder()
                .token(refreshToken)
                .user(user)
                .expiryDate(Instant.now().plusSeconds(30 * 24 * 60 * 60)) // 30 days
                .userAgent(userAgent != null ? userAgent : "Unknown")
                .ipAddress(ipAddress != null ? ipAddress : "Unknown")
                .createdAt(Instant.now())
                .build();

        // Save to database
        refreshTokenRepository.save(tokenEntity);

        return refreshToken;
    }

    @Transactional
    public TokenRefreshResponseDTO refreshToken(String refreshToken) {
        Optional<RefreshToken> tokenOpt = refreshTokenRepository.findByToken(refreshToken);

        if (tokenOpt.isEmpty()) {
            throw new RuntimeException("Invalid refresh token");
        }

        RefreshToken token = tokenOpt.get();

        if (token.getExpiryDate().isBefore(Instant.now())) {
            refreshTokenRepository.delete(token);
            throw new RuntimeException("Refresh token has expired");
        }

        // Generate new access token
        String newAccessToken = jwtProvider.generateToken(
                new org.springframework.security.authentication.UsernamePasswordAuthenticationToken(
                        token.getUser().getEmail(),
                        null
                )
        );

        return TokenRefreshResponseDTO.builder()
                .accessToken(newAccessToken)
                .refreshToken(refreshToken)
                .build();
    }

    @Transactional
    public void revokeToken(String refreshToken) {
        Optional<RefreshToken> tokenOpt = refreshTokenRepository.findByToken(refreshToken);
        tokenOpt.ifPresent(refreshTokenRepository::delete);
    }

    private String getClientIpAddress(HttpServletRequest request) {
        String xForwardedFor = request.getHeader("X-Forwarded-For");
        if (xForwardedFor != null && !xForwardedFor.isEmpty() && !"unknown".equalsIgnoreCase(xForwardedFor)) {
            return xForwardedFor.split(",")[0];
        }
        String xRealIp = request.getHeader("X-Real-IP");
        if (xRealIp != null && !xRealIp.isEmpty() && !"unknown".equalsIgnoreCase(xRealIp)) {
            return xRealIp;
        }
        return request.getRemoteAddr();
    }
}

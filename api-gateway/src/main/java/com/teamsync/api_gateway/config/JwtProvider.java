package com.teamsync.api_gateway.config;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import javax.crypto.SecretKey;
import java.util.Date;

@Service
public class JwtProvider {
    private static final Logger log = LoggerFactory.getLogger(JwtProvider.class);
    
    private final SecretKey key;

    public JwtProvider(JwtConstant jwtConstant) {
        String secretKey = jwtConstant.getSecretKey();
        log.debug("Creating JWT provider with secret key length: {}", secretKey != null ? secretKey.length() : 0);
        this.key = Keys.hmacShaKeyFor(secretKey.getBytes());
        log.debug("JWT provider initialized successfully");
    }

    public String getEmailFromToken(String jwt) {
        try {
            if (jwt.startsWith("Bearer ")) {
                jwt = jwt.substring(7);
            }
            log.debug("Extracting email from JWT token");
            
            String email = Jwts.parser()
                    .verifyWith(key)
                    .build()
                    .parseSignedClaims(jwt)
                    .getPayload()
                    .get("email", String.class);
            
            log.debug("Email extracted from JWT token: {}", email);
            return email;
        } catch (Exception e) {
            log.warn("Failed to extract email from JWT token: {}", e.getMessage());
            return null;
        }
    }

    public boolean validateToken(String jwt) {
        try {
            if (jwt.startsWith("Bearer ")) {
                jwt = jwt.substring(7);
            }
            log.debug("Validating JWT token, length: {}", jwt.length());
            
            // Try to parse claims to get more details
            try {
                Claims claims = Jwts.parser()
                        .verifyWith(key)
                        .build()
                        .parseSignedClaims(jwt)
                        .getPayload();
                
                if (claims.getExpiration() != null) {
                    log.debug("JWT token expiration: {}", claims.getExpiration());
                    if (claims.getExpiration().before(new Date())) {
                        log.warn("JWT token has expired");
                        return false;
                    }
                }
                
                if (claims.get("email") != null) {
                    log.debug("JWT token email: {}", claims.get("email"));
                }
                
            } catch (Exception parseEx) {
                log.warn("Failed to parse JWT claims: {}", parseEx.getMessage());
                return false;
            }
            
            // Now validate the signature
            Jwts.parser()
                    .verifyWith(key)
                    .build()
                    .parseSignedClaims(jwt);
            
            log.debug("JWT token validation successful");
            return true;
        } catch (Exception e) {
            log.warn("JWT token validation failed: {}", e.getMessage());
            return false;
        }
    }

    public Date getExpirationDateFromToken(String jwt) {
        if (jwt.startsWith("Bearer ")) {
            jwt = jwt.substring(7);
        }
        Claims claims = Jwts.parser()
                .verifyWith(key)
                .build()
                .parseSignedClaims(jwt)
                .getPayload();
        return claims.getExpiration();
    }

    public Claims getClaimsFromToken(String jwt) {
        if (jwt.startsWith("Bearer ")) {
            jwt = jwt.substring(7);
        }
        return Jwts.parser()
                .verifyWith(key)
                .build()
                .parseSignedClaims(jwt)
                .getPayload();
    }
}

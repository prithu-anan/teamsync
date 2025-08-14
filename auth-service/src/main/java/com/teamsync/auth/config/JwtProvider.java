package com.teamsync.auth.config;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.stereotype.Service;

import javax.crypto.SecretKey;
import java.util.Date;
import java.util.stream.Collectors;

@Service
public class JwtProvider {
    private final SecretKey key;

    public JwtProvider(JwtConstant jwtConstant) {
        this.key = Keys.hmacShaKeyFor(jwtConstant.getSecretKey().getBytes());
    }

    public String generateToken(Authentication auth) {
        // Build JWT token
        String jwt = Jwts.builder()
                .setIssuedAt(new Date())
                .setExpiration(new Date(new Date().getTime() + 12000000))
                .claim("email", auth.getName())
                .claim("userId", getUserIdFromAuthentication(auth))
                .claim("authorities", auth.getAuthorities().stream()
                        .map(GrantedAuthority::getAuthority)
                        .collect(Collectors.joining(",")))
                .signWith(key)
                .compact();

        return jwt;
    }

    private Long getUserIdFromAuthentication(Authentication auth) {
        // Extract user ID from authentication principal
        if (auth.getPrincipal() instanceof org.springframework.security.core.userdetails.User) {
            org.springframework.security.core.userdetails.User userDetails = 
                (org.springframework.security.core.userdetails.User) auth.getPrincipal();
            // We need to get the user ID from the database using email
            // This will be handled in the AuthService
            return null; // Will be set by AuthService
        }
        return null;
    }

    public String getEmailFromToken(String jwt) {
        if (jwt.startsWith("Bearer ")) {
            jwt = jwt.substring(7);
        }
        return Jwts.parser()
                .verifyWith(key)
                .build()
                .parseSignedClaims(jwt)
                .getPayload()
                .get("email", String.class);
    }

    public Long getUserIdFromToken(String jwt) {
        if (jwt.startsWith("Bearer ")) {
            jwt = jwt.substring(7);
        }
        return Jwts.parser()
                .verifyWith(key)
                .build()
                .parseSignedClaims(jwt)
                .getPayload()
                .get("userId", Long.class);
    }

    public boolean validateToken(String jwt) {
        try {
            if (jwt.startsWith("Bearer ")) {
                jwt = jwt.substring(7);
            }
            Jwts.parser()
                    .verifyWith(key)
                    .build()
                    .parseSignedClaims(jwt);
            return true;
        } catch (Exception e) {
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

    public SecretKey getKey() {
        return key;
    }
}

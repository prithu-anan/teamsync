package com.teamsync.api_gateway.config;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;
import org.springframework.stereotype.Service;

import javax.crypto.SecretKey;
import java.util.Date;

@Service
public class JwtProvider {
    private final SecretKey key;

    public JwtProvider(JwtConstant jwtConstant) {
        this.key = Keys.hmacShaKeyFor(jwtConstant.getSecretKey().getBytes());
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

package com.teamsync.notificationservice.config;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;
import org.springframework.stereotype.Component;

import javax.crypto.SecretKey;
import java.util.Date;

@Component
public class JwtProvider {

    private final JwtConstant jwtConstant;
    private final SecretKey key;

    public JwtProvider(JwtConstant jwtConstant) {
        this.jwtConstant = jwtConstant;
        this.key = Keys.hmacShaKeyFor(jwtConstant.getSecretKey().getBytes());
    }

    public boolean validateToken(String token) {
        try {
            if (token.startsWith("Bearer ")) token = token.substring(7);
            Jwts.parser().verifyWith(key).build().parseSignedClaims(token);
            return true;
        } catch (Exception e) {
            return false;
        }
    }

    public String getEmailFromToken(String token) {
        try {
            if (token.startsWith("Bearer ")) token = token.substring(7);
            Claims claims = Jwts.parser().verifyWith(key).build().parseSignedClaims(token).getPayload();
            return claims.get("email", String.class);
        } catch (Exception e) {
            return null;
        }
    }

    public String getSubjectFromToken(String token) {
        try {
            if (token.startsWith("Bearer ")) token = token.substring(7);
            Claims claims = Jwts.parser().verifyWith(key).build().parseSignedClaims(token).getPayload();
            return claims.getSubject();
        } catch (Exception e) {
            return null;
        }
    }

    public Date getExpirationDateFromToken(String token) {
        try {
            if (token.startsWith("Bearer ")) token = token.substring(7);
            Claims claims = Jwts.parser().verifyWith(key).build().parseSignedClaims(token).getPayload();
            return claims.getExpiration();
        } catch (Exception e) {
            return null;
        }
    }

    public boolean isTokenExpired(String token) {
        Date expiration = getExpirationDateFromToken(token);
        return expiration != null && expiration.before(new Date());
    }
}

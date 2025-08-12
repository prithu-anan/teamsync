package com.teamsync.auth.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

@Component
public class JwtConstant {
    @Value("${jwt.secret}")
    private String SECRET_KEY;

    public static final String JWT_HEADER = "Authorization";

    // Getter for SECRET_KEY
    public String getSecretKey() {
        return SECRET_KEY;
    }
}

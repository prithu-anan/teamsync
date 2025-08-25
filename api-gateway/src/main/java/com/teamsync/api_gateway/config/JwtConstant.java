package com.teamsync.api_gateway.config;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

@Component
public class JwtConstant {
    private static final Logger log = LoggerFactory.getLogger(JwtConstant.class);
    
    @Value("${jwt.secret}")
    private String SECRET_KEY;

    public static final String JWT_HEADER = "Authorization";

    // Getter for SECRET_KEY
    public String getSecretKey() {
        log.debug("JWT secret key loaded, length: {}", SECRET_KEY != null ? SECRET_KEY.length() : 0);
        return SECRET_KEY;
    }
}

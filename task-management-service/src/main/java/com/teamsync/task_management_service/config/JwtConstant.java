package com.teamsync.task_management_service.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import jakarta.annotation.PostConstruct;

@Component
public class JwtConstant {
    private static final Logger log = LoggerFactory.getLogger(JwtConstant.class);
    
    @Value("${jwt.secret}")
    private String SECRET_KEY;
    
    @Value("${jwt.expiration}")
    private long EXPIRATION_TIME;
    
    public static final String JWT_HEADER = "Authorization";
    
    @PostConstruct
    public void logConfiguration() {
        log.debug("JWT Configuration loaded - Secret key length: {}, Expiration: {} ms", 
                 SECRET_KEY != null ? SECRET_KEY.length() : 0, EXPIRATION_TIME);
    }
    
    // Getters for the injected values
    public String getSecretKey() {
        return SECRET_KEY;
    }
    
    public long getExpirationTime() {
        return EXPIRATION_TIME;
    }
}

package com.teamsync.message_management_service.config;

import feign.RequestInterceptor;
import feign.RequestTemplate;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.beans.factory.annotation.Autowired;

@Configuration
public class FeignConfig {

    private static final Logger log = LoggerFactory.getLogger(FeignConfig.class);

    @Autowired
    private JwtProvider jwtProvider;

    @Bean
    public RequestInterceptor requestInterceptor() {
        return new RequestInterceptor() {
            @Override
            public void apply(RequestTemplate template) {
                // Generate a service token for internal service calls
                String token = jwtProvider.generateServiceToken();
                log.debug("Adding service token to Feign request: {}", token.substring(0, Math.min(20, token.length())) + "...");
                log.debug("Full token: {}", token);
                log.debug("Token subject: {}", jwtProvider.getSubjectFromToken(token));
                log.debug("Token validation: {}", jwtProvider.validateToken(token));
                log.debug("Token expired: {}", jwtProvider.isTokenExpired(token));
                template.header("Authorization", "Bearer " + token);
            }
        };
    }
}

package com.teamsync.api_gateway.service;

import com.fasterxml.jackson.annotation.JsonProperty;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Mono;

class TokenValidationResponse {
    @JsonProperty("data")
    private Boolean data;
    
    public Boolean getData() { return data; }
    public void setData(Boolean data) { this.data = data; }
}

@Service
public class AuthServiceClient {
    private static final Logger log = LoggerFactory.getLogger(AuthServiceClient.class);
    
    private final WebClient webClient;
    
    public AuthServiceClient(@Value("${auth.service.url:http://auth-service:8081}") String authServiceUrl) {
        this.webClient = WebClient.builder()
                .baseUrl(authServiceUrl)
                .build();
    }
    
    public Mono<Boolean> validateToken(String token) {
        return webClient.get()
                .uri("/auth/validate")
                .header("Authorization", "Bearer " + token)
                .retrieve()
                .bodyToMono(TokenValidationResponse.class)
                .map(response -> {
                    if (response.getData() != null) {
                        return response.getData();
                    } else {
                        log.warn("Token validation response has null data");
                        return false;
                    }
                })
                .onErrorReturn(false)
                .timeout(java.time.Duration.ofSeconds(5)) // Add timeout to prevent hanging
                .retry(1) // Retry once on failure
                .doOnError(error -> log.error("Token validation failed: {}", error.getMessage()));
    }
    
    public Mono<String> getCurrentUser(String token) {
        return webClient.get()
                .uri("/auth/me")
                .header("Authorization", "Bearer " + token)
                .retrieve()
                .bodyToMono(String.class)
                .onErrorReturn("");
    }
    
    public Mono<Boolean> isTokenBlacklisted(String token) {
        return webClient.get()
                .uri("/auth/blacklist/check")
                .header("Authorization", "Bearer " + token)
                .retrieve()
                .bodyToMono(Boolean.class)
                .onErrorReturn(false);
    }
}

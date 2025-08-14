package com.teamsync.api_gateway.service;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Mono;

@Service
public class AuthServiceClient {
    
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
                .bodyToMono(Boolean.class)
                .onErrorReturn(false);
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

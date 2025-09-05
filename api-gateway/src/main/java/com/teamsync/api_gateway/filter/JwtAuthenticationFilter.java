package com.teamsync.api_gateway.filter;

import com.teamsync.api_gateway.config.JwtConstant;
import com.teamsync.api_gateway.config.JwtProvider;
import com.teamsync.api_gateway.service.AuthServiceClient;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.util.Collections;

@Component
public class JwtAuthenticationFilter extends OncePerRequestFilter {

    private static final Logger log = LoggerFactory.getLogger(JwtAuthenticationFilter.class);
    
    private final JwtProvider jwtProvider;
    private final JwtConstant jwtConstant;
    private final AuthServiceClient authServiceClient;

    public JwtAuthenticationFilter(JwtProvider jwtProvider, JwtConstant jwtConstant, AuthServiceClient authServiceClient) {
        this.jwtProvider = jwtProvider;
        this.jwtConstant = jwtConstant;
        this.authServiceClient = authServiceClient;
    }

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain)
            throws ServletException, IOException {
        
        try {
            String jwt = getJwtFromRequest(request);
            
            if (StringUtils.hasText(jwt)) {
                log.debug("JWT token found in request, length: {}", jwt.length());
                // Log first and last few characters for debugging (without exposing full token)
                if (jwt.length() > 20) {
                    log.debug("JWT token preview: {}...{}", jwt.substring(0, 10), jwt.substring(jwt.length() - 10));
                } else {
                    log.debug("JWT token: {}", jwt);
                }
                
                if (jwtProvider.validateToken(jwt)) {
                    log.debug("JWT token structure is valid");
                    
                    // First, validate locally for basic JWT structure
                    String email = jwtProvider.getEmailFromToken(jwt);
                    
                    if (email != null && !email.trim().isEmpty()) {
                        log.debug("Email extracted from token: {}", email);
                        
                        // Now check with auth service for blacklist status and additional validation
                        try {
                            log.debug("Calling auth service to validate token and check blacklist");
                            Boolean isValid = authServiceClient.validateToken(jwt).block();
                            
                            if (isValid != null && isValid) {
                                // Create authentication token
                                UsernamePasswordAuthenticationToken authentication = new UsernamePasswordAuthenticationToken(
                                    email, null, Collections.singletonList(new SimpleGrantedAuthority("USER")));
                                
                                SecurityContextHolder.getContext().setAuthentication(authentication);
                                log.debug("Authentication set for user: {} (token validated with blacklist check)", email);
                            } else {
                                log.warn("Token validation failed or token is blacklisted for user: {}", email);
                                // Return 403 Forbidden with error message for invalid/blacklisted tokens
                                response.setStatus(HttpServletResponse.SC_FORBIDDEN);
                                response.setContentType("application/json");
                                response.setCharacterEncoding("UTF-8");
                                response.getWriter().write("{\"error\":\"Token validation failed\",\"message\":\"Token is invalid or has been blacklisted\"}");
                                return;
                            }
                        } catch (Exception authServiceEx) {
                            log.warn("Auth service validation failed for user: {}, falling back to local validation. Error: {}", 
                                    email, authServiceEx.getMessage());
                            
                            // Fallback: if auth service is unavailable, still allow the request
                            // but log the security risk
                            UsernamePasswordAuthenticationToken authentication = new UsernamePasswordAuthenticationToken(
                                email, null, Collections.singletonList(new SimpleGrantedAuthority("USER")));
                            
                            SecurityContextHolder.getContext().setAuthentication(authentication);
                            log.warn("Authentication set with fallback (no blacklist check) for user: {}", email);
                        }
                    } else {
                        log.warn("Could not extract email from token");
                    }
                } else {
                    log.warn("JWT token structure is invalid");
                }
            } else {
                log.debug("No JWT token found in request");
            }
        } catch (Exception ex) {
            log.error("Could not set user authentication in security context", ex);
        }
        
        filterChain.doFilter(request, response);
    }

    private String getJwtFromRequest(HttpServletRequest request) {
        String bearerToken = request.getHeader(jwtConstant.JWT_HEADER);
        if (StringUtils.hasText(bearerToken) && bearerToken.startsWith("Bearer ")) {
            return bearerToken.substring(7);
        }
        return null;
    }
}

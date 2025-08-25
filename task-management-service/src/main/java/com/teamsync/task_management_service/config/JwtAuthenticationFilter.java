package com.teamsync.task_management_service.config;

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

    public JwtAuthenticationFilter(JwtProvider jwtProvider) {
        this.jwtProvider = jwtProvider;
    }

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain)
            throws ServletException, IOException {
        
        try {
            String jwt = getJwtFromRequest(request);
            
            if (StringUtils.hasText(jwt)) {
                log.debug("JWT token found in request, length: {}", jwt.length());
                log.debug("JWT token starts with: {}", jwt.substring(0, Math.min(20, jwt.length())));
                
                if (jwtProvider.validateToken(jwt) && !jwtProvider.isTokenExpired(jwt)) {
                    log.debug("JWT token is valid and not expired");
                    // First try to extract email (for user tokens)
                    String email = jwtProvider.getEmailFromToken(jwt);
                    
                    if (StringUtils.hasText(email)) {
                        log.debug("Email extracted from token: {}", email);
                        
                        // Create authentication token for user
                        UsernamePasswordAuthenticationToken authentication = new UsernamePasswordAuthenticationToken(
                            email, null, Collections.singletonList(new SimpleGrantedAuthority("USER")));
                        
                        SecurityContextHolder.getContext().setAuthentication(authentication);
                        log.debug("Authentication set for user: {}", email);
                    } else {
                        log.debug("No email found in token, trying to extract subject");
                        // If no email, try to extract subject (for service tokens)
                        String subject = jwtProvider.getSubjectFromToken(jwt);
                        log.debug("Subject extracted from token: {}", subject);
                        if (StringUtils.hasText(subject) && subject.endsWith("-service")) {
                            log.debug("Service token detected for service: {}", subject);
                            
                            // Create authentication token for service
                            UsernamePasswordAuthenticationToken authentication = new UsernamePasswordAuthenticationToken(
                                subject, null, Collections.singletonList(new SimpleGrantedAuthority("SERVICE")));
                            
                            SecurityContextHolder.getContext().setAuthentication(authentication);
                            log.debug("Authentication set for service: {}", subject);
                        } else {
                            log.warn("Could not extract email or valid service identifier from token. Subject: {}", subject);
                        }
                    }
                } else {
                    log.warn("JWT token is invalid or expired. Valid: {}, Expired: {}", 
                             jwtProvider.validateToken(jwt), jwtProvider.isTokenExpired(jwt));
                }
            } else {
                log.debug("No JWT token found in request");
                log.debug("Request headers: {}", Collections.list(request.getHeaderNames()));
            }
        } catch (Exception ex) {
            log.error("Could not set user authentication in security context", ex);
        }
        
        filterChain.doFilter(request, response);
    }

    private String getJwtFromRequest(HttpServletRequest request) {
        String bearerToken = request.getHeader(JwtConstant.JWT_HEADER);
        if (StringUtils.hasText(bearerToken) && bearerToken.startsWith("Bearer ")) {
            return bearerToken.substring(7);
        }
        return null;
    }
}

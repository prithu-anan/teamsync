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
            
            if (StringUtils.hasText(jwt) && jwtProvider.validateToken(jwt)) {
                // Use the validate endpoint to check both token validity and blacklist status
                Boolean isValid = authServiceClient.validateToken(jwt).block();
                
                if (isValid != null && isValid) {
                    String email = jwtProvider.getEmailFromToken(jwt);
                    
                    // Create authentication token
                    UsernamePasswordAuthenticationToken authentication = new UsernamePasswordAuthenticationToken(
                        email, null, Collections.singletonList(new SimpleGrantedAuthority("USER")));
                    
                    SecurityContextHolder.getContext().setAuthentication(authentication);
                } else {
                    log.warn("Invalid or blacklisted token used in API Gateway: {}", jwt);
                }
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

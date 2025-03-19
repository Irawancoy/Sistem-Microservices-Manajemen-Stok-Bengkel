package com.microservices.smmsb_inventory_service.security;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.web.authentication.WebAuthenticationDetailsSource;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import lombok.extern.slf4j.Slf4j;

import java.io.IOException;
import java.util.Collections;

@Component
@Slf4j
public class GatewayAuthenticationFilter extends OncePerRequestFilter {

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain)
            throws ServletException, IOException {
        
        // Cek header autentikasi dari API Gateway
        String username = request.getHeader("X-Authenticated-User");
        String userId = request.getHeader("X-User-Id");
        String role = request.getHeader("X-User-Role");
        
        // Jika header autentikasi ada, buat autentikasi
        if (username != null && userId != null && role != null) {
            // Buat autentikasi dengan peran yang sesuai
            UsernamePasswordAuthenticationToken authentication = new UsernamePasswordAuthenticationToken(
                username, 
                null, 
                Collections.singletonList(new SimpleGrantedAuthority(role))
            );
            
            authentication.setDetails(new WebAuthenticationDetailsSource().buildDetails(request));
            SecurityContextHolder.getContext().setAuthentication(authentication);
            
            log.debug("User authenticated via API Gateway: {}", username);
        }
        
        filterChain.doFilter(request, response);
    }
}

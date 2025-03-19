package com.microservices.smmsb_user_service.security;

import java.io.IOException;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.extern.slf4j.Slf4j;

@Component
@Slf4j
public class ApiGatewayHeaderFilter extends OncePerRequestFilter {

    @Value("${gateway.header.name:X-Gateway-Access}")
    private String gatewayHeaderName;

    @Value("${gateway.header.value:enabled}")
    private String gatewayHeaderValue;

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain)
            throws ServletException, IOException {

        String path = request.getRequestURI();

        // Skip check for Swagger paths
        if (path.startsWith("/user-service/swagger-ui") ||
                path.startsWith("/user-service/v3/api-docs")) {
            filterChain.doFilter(request, response);
            return;
        }

        // Only check gateway header for /api/v1/users/ endpoint
        if (path.startsWith("/api/v1/users/")) {

            // Check if the request has the gateway header
            String gatewayHeader = request.getHeader(gatewayHeaderName);

            if (gatewayHeader == null || !gatewayHeaderValue.equals(gatewayHeader)) {
                log.warn("Unauthorized direct access attempt to auth endpoint: {}", path);
                response.setStatus(HttpStatus.FORBIDDEN.value());
                response.getWriter().write("Access denied: This endpoint can only be accessed through the API Gateway");
                return;
            }
        }

        filterChain.doFilter(request, response);
    }
}

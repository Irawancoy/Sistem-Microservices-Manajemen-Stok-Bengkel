package com.microservices.smmsb_api_gateway.config;

import org.springframework.cloud.gateway.route.RouteLocator;
import org.springframework.cloud.gateway.route.builder.RouteLocatorBuilder;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class ApiGatewayConfig {

    @Bean
    public RouteLocator gatewayRoutes(RouteLocatorBuilder builder) {
        return builder.routes()
            // Route untuk Auth Service (Login & Register)
            .route("auth-service", r -> r.path("/api/v1/auth/**")
                .uri("lb://SMMSB-USER-SERVICE")) // Load Balancer ke Eureka
            
            // Route untuk User Service (User Management)
            .route("user-service", r -> r.path("/api/v1/users/**")
                .uri("lb://SMMSB-USER-SERVICE")) // Load Balancer ke Eureka

                // Route untuk Inventory Service (Inventory Management)
            .route("inventory-service", r -> r.path("/api/v1/product-stock/**")
                .uri("lb://SMMSB-INVENTORY-SERVICE")) // Load Balancer ke Eureka

                // Route untuk Transaction Service (Transaction Management)
            .route("transaction-service", r -> r.path("/api/v1/transactions/**")
            .uri("lb://SMMSB-TRANSACTION-SERVICE")) // Load Balancer ke Eureka

            .build();
    }
}

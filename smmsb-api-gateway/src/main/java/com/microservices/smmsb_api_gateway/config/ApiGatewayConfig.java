package com.microservices.smmsb_api_gateway.config;

import com.microservices.smmsb_api_gateway.security.JwtAuthGatewayFilterFactory;

import org.springframework.cloud.gateway.route.RouteLocator;
import org.springframework.cloud.gateway.route.builder.RouteLocatorBuilder;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.beans.factory.annotation.Value;

@Configuration
public class ApiGatewayConfig {

        private final JwtAuthGatewayFilterFactory jwtAuthFilterFactory;

        @Value("${gateway.header.name:X-Gateway-Access}")
        private String gatewayHeaderName;

        @Value("${gateway.header.value:enabled}")
        private String gatewayHeaderValue;

        public ApiGatewayConfig(JwtAuthGatewayFilterFactory jwtAuthFilterFactory) {
                this.jwtAuthFilterFactory = jwtAuthFilterFactory;
        }

        @Bean
        public RouteLocator gatewayRoutes(RouteLocatorBuilder builder) {
                return builder.routes()
                                // Route untuk Auth Service (Login & Register) (Tanpa JWT)
                                .route("auth-service", r -> r.path("/api/v1/auth/**")
                                                .filters(f -> f.addRequestHeader(gatewayHeaderName, gatewayHeaderValue))
                                                .uri("lb://SMMSB-USER-SERVICE"))

                                // Route untuk User Service (Dengan JWT)
                                .route("user-service", r -> r.path("/api/v1/users/**")
                                                .filters(f -> f
                                                                .filter(jwtAuthFilterFactory.apply(
                                                                                new JwtAuthGatewayFilterFactory.Config()))
                                                                .addRequestHeader(gatewayHeaderName,
                                                                                gatewayHeaderValue))
                                                .uri("lb://SMMSB-USER-SERVICE"))

                                // Route untuk Inventory Service (Dengan JWT)
                                .route("inventory-service", r -> r.path("/api/v1/inventory/**")
                                                .filters(f -> f
                                                                .filter(jwtAuthFilterFactory.apply(
                                                                                new JwtAuthGatewayFilterFactory.Config()))
                                                                .addRequestHeader(gatewayHeaderName,
                                                                                gatewayHeaderValue))
                                                .uri("lb://SMMSB-INVENTORY-SERVICE"))

                                // Route untuk Transaction Service (Dengan JWT + Role Check)
                                .route("transaction-service", r -> r.path("/api/v1/transactions/**")
                                                .filters(f -> f
                                                                .filter(jwtAuthFilterFactory.apply(
                                                                                new JwtAuthGatewayFilterFactory.Config()))
                                                                .addRequestHeader(gatewayHeaderName,
                                                                                gatewayHeaderValue))
                                                .uri("lb://SMMSB-TRANSACTION-SERVICE"))

                                // Route untuk Notification Service (Dengan JWT)
                                .route("notification-service", r -> r.path("/api/v1/notifications/**")
                                                .filters(f -> f
                                                                .filter(jwtAuthFilterFactory.apply(
                                                                                new JwtAuthGatewayFilterFactory.Config()))
                                                                .addRequestHeader(gatewayHeaderName,
                                                                                gatewayHeaderValue))
                                                .uri("lb://SMMSB-NOTIFICATION-SERVICE"))

                                // Route Index Swagger
                                .route("swagger-index", r -> r.path("/swagger-index")
                                                .uri("lb://SMMSB-API-GATEWAY"))

                                // ===== USER SERVICE SWAGGER - DIRECT PASS-THROUGH =====
                                // User Service has custom paths configured, so we pass these through directly
                                .route("user-service-swagger-ui", r -> r.path("/user-service/swagger-ui.html",
                                                "/user-service/swagger-ui/**")
                                                .filters(f -> f.addRequestHeader(gatewayHeaderName, gatewayHeaderValue))
                                                .uri("lb://SMMSB-USER-SERVICE"))

                                // User Service API Docs - direct pass-through
                                .route("user-service-api-docs", r -> r.path("/user-service/v3/api-docs/**")
                                                .filters(f -> f.addRequestHeader(gatewayHeaderName, gatewayHeaderValue))
                                                .uri("lb://SMMSB-USER-SERVICE"))

                                // ===== TRANSACTION SERVICE SWAGGER - DIRECT PASS-THROUGH =====
                                // Transaction Service has custom paths configured, so we pass these through
                                // directly
                                .route("transaction-service-swagger-ui", r -> r
                                                .path("/transaction-service/swagger-ui.html",
                                                                "/transaction-service/swagger-ui/**")
                                                .filters(f -> f.addRequestHeader(gatewayHeaderName, gatewayHeaderValue))
                                                .uri("lb://SMMSB-TRANSACTION-SERVICE"))

                                .route("transaction-service-api-docs", r -> r
                                                .path("/transaction-service/v3/api-docs/**")
                                                .filters(f -> f.addRequestHeader(gatewayHeaderName, gatewayHeaderValue))
                                                .uri("lb://SMMSB-TRANSACTION-SERVICE"))

                                // ===== INVENTORY SERVICE SWAGGER - DIRECT PASS-THROUGH =====
                                // Inventory Service has custom paths configured, so we pass these through
                                // directly
                                .route("inventory-service-swagger-ui", r -> r
                                                .path("/inventory-service/swagger-ui.html",
                                                                "/inventory-service/swagger-ui/**")
                                                .filters(f -> f.addRequestHeader(gatewayHeaderName, gatewayHeaderValue))
                                                .uri("lb://SMMSB-INVENTORY-SERVICE"))

                                .route("inventory-service-api-docs", r -> r
                                                .path("/inventory-service/v3/api-docs/**")
                                                .filters(f -> f.addRequestHeader(gatewayHeaderName, gatewayHeaderValue))
                                                .uri("lb://SMMSB-INVENTORY-SERVICE"))

                                // ===== NOTIFICATION SERVICE SWAGGER - DIRECT PASS-THROUGH =====
                                // Notification Service has custom paths configured, so we pass these through
                                // directly
                                .route("notification-service-swagger-ui", r -> r
                                                .path("/notification-service/swagger-ui.html",
                                                                "/notification-service/swagger-ui/**")
                                                .filters(f -> f.addRequestHeader(gatewayHeaderName, gatewayHeaderValue))
                                                .uri("lb://SMMSB-NOTIFICATION-SERVICE"))

                                .route("notification-service-api-docs", r -> r
                                                .path("/notification-service/v3/api-docs/**")
                                                .filters(f -> f.addRequestHeader(gatewayHeaderName, gatewayHeaderValue))
                                                .uri("lb://SMMSB-NOTIFICATION-SERVICE"))
                                .build();
        }
}

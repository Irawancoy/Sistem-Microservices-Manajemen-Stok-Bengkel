package com.microservices.smmsb_api_gateway.security;

import org.springframework.cloud.gateway.filter.GatewayFilter;
import org.springframework.cloud.gateway.filter.factory.AbstractGatewayFilterFactory;
import org.springframework.stereotype.Component;
import org.springframework.beans.factory.annotation.Autowired;

@Component
public class JwtAuthGatewayFilterFactory extends AbstractGatewayFilterFactory<JwtAuthGatewayFilterFactory.Config> {

    private final JwtAuthFilter jwtAuthFilter;

    @Autowired
    public JwtAuthGatewayFilterFactory(JwtAuthFilter jwtAuthFilter) {
        super(Config.class);
        this.jwtAuthFilter = jwtAuthFilter;
    }

    @Override
    public GatewayFilter apply(Config config) {
        return (exchange, chain) -> jwtAuthFilter.filter(exchange, chain);
    }

    public static class Config {
        // Konfigurasi tambahan jika diperlukan
    }
}

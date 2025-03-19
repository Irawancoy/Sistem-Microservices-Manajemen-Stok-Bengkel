//securityconfig transaction service
package com.microservices.smmsb_transaction_service.security;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.Customizer;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;

@Configuration
@EnableWebSecurity
@EnableMethodSecurity
public class SecurityConfig {
      @Autowired
      private ApiGatewayHeaderFilter apiGatewayHeaderFilter;

      @Autowired
      private GatewayAuthenticationFilter gatewayAuthenticationFilter;

      @Bean
      public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
            return http
                        .cors(Customizer.withDefaults())
                        .csrf(AbstractHttpConfigurer::disable)
                        .sessionManagement(sess -> sess.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
                        .authorizeHttpRequests(auth -> auth
                                    // Public endpoints - still protected by API Gateway header
                                    .requestMatchers(

                                                // New custom Swagger paths
                                                "/transaction-service/swagger-ui.html",
                                                "/transaction-service/swagger-ui/**",
                                                "/transaction-service/v3/api-docs/**")
                                    .permitAll()
                                    // All other endpoints require authentication
                                    .anyRequest().authenticated())
                        .addFilterBefore(apiGatewayHeaderFilter, UsernamePasswordAuthenticationFilter.class)
                        .addFilterBefore(gatewayAuthenticationFilter, UsernamePasswordAuthenticationFilter.class)

                        .build();
      }

}
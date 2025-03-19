package com.microservices.smmsb_user_service.security;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.AuthenticationProvider;
import org.springframework.security.authentication.dao.DaoAuthenticationProvider;
import org.springframework.security.config.Customizer;
import org.springframework.security.config.annotation.authentication.configuration.AuthenticationConfiguration;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;
import com.microservices.smmsb_user_service.service.impl.UserDetailsServiceImpl;

@Configuration
@EnableWebSecurity
@EnableMethodSecurity
public class SecurityConfig {

   @Autowired
   private ApiGatewayHeaderFilter apiGatewayHeaderFilter;

   @Autowired
   private GatewayAuthenticationFilter gatewayAuthenticationFilter;

   @Bean
   public UserDetailsService userDetailsService() {
      return new UserDetailsServiceImpl();
   }

   @Bean
   public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
      return http
            .cors(Customizer.withDefaults())
            .csrf(AbstractHttpConfigurer::disable)
            .sessionManagement(sess -> sess.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
            .authorizeHttpRequests(auth -> auth
                  // Public endpoints - still protected by API Gateway header
                  .requestMatchers(
                        "/api/v1/auth/login",

                        // New custom Swagger paths
                        "/user-service/swagger-ui.html",
                        "/user-service/swagger-ui/**",
                        "/user-service/v3/api-docs/**")
                  .permitAll()
                  // All other endpoints require authentication
                  .anyRequest().authenticated())
            .addFilterBefore(apiGatewayHeaderFilter, UsernamePasswordAuthenticationFilter.class)
            .addFilterBefore(gatewayAuthenticationFilter, UsernamePasswordAuthenticationFilter.class)
            .build();
   }

   @Bean
   public PasswordEncoder passwordEncoder() {
      return new BCryptPasswordEncoder();
   }

   @Bean
   public AuthenticationProvider authenticationProvider() {
      DaoAuthenticationProvider authenticationProvider = new DaoAuthenticationProvider();
      authenticationProvider.setUserDetailsService(userDetailsService());
      authenticationProvider.setPasswordEncoder(passwordEncoder());
      return authenticationProvider;
   }

   @Bean
   public AuthenticationManager authenticationManager(AuthenticationConfiguration config)
         throws Exception {
      return config.getAuthenticationManager();
   }
}

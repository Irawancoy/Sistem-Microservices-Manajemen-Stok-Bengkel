package com.microservices.smmsb_user_service.security;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
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
   private JwtAuthFilter jwtAuthFilter;

   // User Creation
   @Bean
   public UserDetailsService userDetailsService() {
      return new UserDetailsServiceImpl();
   }

   @Bean
   public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
       return http
           .cors(Customizer.withDefaults()) // Aktifkan CORS
           .csrf(AbstractHttpConfigurer::disable)
           .sessionManagement(sess -> sess.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
           .authorizeHttpRequests(auth -> auth
               // ✅ Endpoints Public (tanpa autentikasi)
               .requestMatchers(
                   "/api/v1/auth/login",
                   "/api/v1/users/exist",
                   "/swagger-ui/**",
                   "/v3/api-docs/**"
               ).permitAll()

               // ✅ ROLE_SUPERADMIN Bisa CRUD user
               .requestMatchers(HttpMethod.POST, "/api/v1/users/**").hasRole("SUPERADMIN")
               .requestMatchers(HttpMethod.PUT, "/api/v1/users/**").hasRole("SUPERADMIN")
               .requestMatchers(HttpMethod.DELETE, "/api/v1/users/**").hasRole("SUPERADMIN")

               // ✅ ROLE_ADMIN hanya bisa membaca data user
               .requestMatchers(HttpMethod.GET, "/api/v1/users/**").hasAnyRole("ADMIN", "SUPERADMIN")

               // ✅ Semua request lain harus autentikasi
               .anyRequest().authenticated()
           )
           .addFilterBefore(jwtAuthFilter, UsernamePasswordAuthenticationFilter.class)
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

package com.microservices.smmsb_api_gateway.security;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.io.Decoders;
import io.jsonwebtoken.security.Keys;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.cloud.gateway.filter.GatewayFilterChain;
import org.springframework.cloud.gateway.filter.GlobalFilter;
import org.springframework.core.Ordered;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.server.reactive.ServerHttpRequest;
import org.springframework.http.server.reactive.ServerHttpResponse;
import org.springframework.stereotype.Component;
import org.springframework.web.server.ServerWebExchange;
import reactor.core.publisher.Mono;
import org.springframework.data.redis.core.StringRedisTemplate;

import java.security.Key;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

@Component
@RequiredArgsConstructor
@Slf4j
public class JwtAuthFilter implements GlobalFilter, Ordered {

   private final StringRedisTemplate redisTemplate;

   @Value("${jwt.secret:3vTzq+9JmZm6WzkBcdh+YsPVf3Xa8nYhCTaXlAeR4zU=}")
   private String secretKey;

   @Value("${gateway.header.name:X-Gateway-Access}")
   private String gatewayHeaderName;

   @Value("${gateway.header.value:enabled}")
   private String gatewayHeaderValue;

   // List peran yang diizinkan untuk mengakses layanan tertentu
   private final List<String> TRANSACTION_SERVICE_ALLOWED_ROLES = Arrays.asList("ROLE_ADMIN", "ROLE_SUPERADMIN");
   private final List<String> USER_SERVICE_ALLOWED_ROLES = Collections.singletonList("ROLE_SUPERADMIN");
   private final List<String> INVENTORY_SERVICE_ALLOWED_ROLES = Arrays.asList("ROLE_ADMIN", "ROLE_SUPERADMIN");
   private final List<String> NOTIFICATION_SERVICE_ALLOWED_ROLES = Arrays.asList("ROLE_ADMIN", "ROLE_SUPERADMIN");

   // Endpoint yang tidak memerlukan autentikasi
   private final Set<String> PUBLIC_ENDPOINTS = new HashSet<>(Arrays.asList(
         "/api/v1/auth/login"));

   // Endpoint pattern untuk Swagger UI dan API docs
   private final List<String> SWAGGER_PATTERNS = Arrays.asList(
         "/user-service/swagger-ui.html",
         "/user-service/swagger-ui/",
         "/user-service/v3/api-docs",
         "/transaction-service/swagger-ui.html",
         "/transaction-service/swagger-ui/",
         "/transaction-service/v3/api-docs",
         "/inventory-service/swagger-ui.html",
         "/inventory-service/swagger-ui/",
         "/inventory-service/v3/api-docs",
         "/notification-service/swagger-ui.html",
         "/notification-service/swagger-ui/",
         "/notification-service/v3/api-docs");

   @Override
   public Mono<Void> filter(ServerWebExchange exchange, GatewayFilterChain chain) {
      ServerHttpRequest request = exchange.getRequest();
      String path = request.getURI().getPath();

      // Log the path for debugging
      log.debug("Processing request for path: {}", path);

      // 1. Bypass filter for public endpoints
      if (isPublicEndpoint(path)) {
         log.debug("Public endpoint detected: {}", path);
         ServerHttpRequest modifiedRequest = request.mutate()
               .header(gatewayHeaderName, gatewayHeaderValue)
               .build();
         return chain.filter(exchange.mutate().request(modifiedRequest).build());
      }

      // 2. Bypass filter for Swagger UI and API docs
      if (isSwaggerEndpoint(path)) {
         log.debug("Swagger endpoint detected: {}", path);
         ServerHttpRequest modifiedRequest = request.mutate()
               .header(gatewayHeaderName, gatewayHeaderValue)
               .build();
         return chain.filter(exchange.mutate().request(modifiedRequest).build());
      }

      // 3. Periksa Session ID
      HttpHeaders headers = request.getHeaders();
      if (!headers.containsKey("X-Session-Id")) {
         return onError(exchange, "Missing Session ID Header", HttpStatus.UNAUTHORIZED);
      }

      String sessionId = headers.getFirst("X-Session-Id");
      String redisKey = "AuthSession:" + sessionId;

      // 4. Ambil token dari Redis
      Map<Object, Object> authSession = redisTemplate.opsForHash().entries(redisKey);
      if (authSession.isEmpty() || !authSession.containsKey("token")) {
         return onError(exchange, "Invalid Session or Token Not Found", HttpStatus.UNAUTHORIZED);
      }

      String token = authSession.get("token").toString();
      String username = authSession.get("username").toString();
      String role = authSession.get("role").toString();

      // 5. Validasi token JWT
      Claims claims;
      try {
         claims = Jwts.parserBuilder()
               .setSigningKey(getSignKey())
               .build()
               .parseClaimsJws(token)
               .getBody();
      } catch (Exception e) {
         log.error("Token validation failed: {}", e.getMessage());
         return onError(exchange, "Invalid JWT token", HttpStatus.UNAUTHORIZED);
      }

      // 6. Pastikan username dalam token sesuai dengan session
      String jwtUsername = claims.getSubject();
      if (!jwtUsername.equals(username)) {
         return onError(exchange, "Token does not match session username", HttpStatus.UNAUTHORIZED);
      }

      // 7. Konversi ID pengguna
      Long userId = Long.parseLong(claims.get("userId").toString());

      // 8. Cek otorisasi berdasarkan peran untuk rute tertentu
      if (!checkRoleBasedAccess(path, role)) {
         log.warn("Access denied: User {} with role {} tried to access protected service at {}", username, role, path);
         return onError(exchange, "Access denied: Insufficient privileges", HttpStatus.FORBIDDEN);
      }

      // 9. Teruskan informasi pengguna ke layanan lain
      ServerHttpRequest modifiedRequest = request.mutate()
            .header("X-Authenticated-User", username)
            .header("X-User-Id", String.valueOf(userId))
            .header("X-User-Role", role)
            .header(gatewayHeaderName, gatewayHeaderValue)
            .build();

      log.info("User {} authenticated successfully with role {}, accessing {}", username, role, path);
      return chain.filter(exchange.mutate().request(modifiedRequest).build());
   }

   private boolean isPublicEndpoint(String path) {
      return PUBLIC_ENDPOINTS.contains(path);
   }

   private boolean isSwaggerEndpoint(String path) {
      return SWAGGER_PATTERNS.stream().anyMatch(path::contains);
   }

   private boolean checkRoleBasedAccess(String path, String role) {
      // Cek akses ke User Service - hanya SUPERADMIN yang diizinkan
      if (path.startsWith("/api/v1/users/")) {
         return USER_SERVICE_ALLOWED_ROLES.contains(role);
      }

      // Cek akses ke layanan transaksi
      if (path.startsWith("/api/v1/transactions/")) {
         return TRANSACTION_SERVICE_ALLOWED_ROLES.contains(role);
      }

      // Cek akses ke layanan inventori
      if (path.startsWith("/api/v1/inventory/")) {
         return INVENTORY_SERVICE_ALLOWED_ROLES.contains(role);
      }

      // Cek akses ke layanan notifikasi
      if (path.startsWith("/api/v1/notifications/")) {
         return NOTIFICATION_SERVICE_ALLOWED_ROLES.contains(role);
      }

      // Default: izinkan akses untuk endpoint lain yang memerlukan autentikasi
      return true;
   }

   private Mono<Void> onError(ServerWebExchange exchange, String err, HttpStatus status) {
      log.warn("Authentication error: {}", err);
      ServerHttpResponse response = exchange.getResponse();
      response.setStatusCode(status);
      return response.setComplete();
   }

   private Key getSignKey() {
      byte[] keyBytes = Decoders.BASE64.decode(secretKey);
      return Keys.hmacShaKeyFor(keyBytes);
   }

   @Override
   public int getOrder() {
      return -1; // Eksekusi lebih awal dalam rantai filter
   }
}

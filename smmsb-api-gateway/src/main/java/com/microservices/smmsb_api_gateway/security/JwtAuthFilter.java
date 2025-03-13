package com.microservices.smmsb_api_gateway.security;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.io.Decoders;
import io.jsonwebtoken.security.Keys;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.cloud.gateway.filter.GlobalFilter;
import org.springframework.cloud.gateway.filter.GatewayFilter;
import org.springframework.cloud.gateway.filter.GatewayFilterChain;
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
import java.util.Map;
import java.util.Arrays;
import java.util.List;

@Component
@RequiredArgsConstructor
@Slf4j
public class JwtAuthFilter implements GlobalFilter, Ordered, GatewayFilter {

   private final StringRedisTemplate redisTemplate;
   private final String SECRET_KEY = "3vTzq+9JmZm6WzkBcdh+YsPVf3Xa8nYhCTaXlAeR4zU=";
   
   // List peran yang diizinkan untuk mengakses layanan transaksi
   private final List<String> TRANSACTION_ALLOWED_ROLES = Arrays.asList("ROLE_ADMIN", "ROLE_SUPERADMIN");

   @Override
   public Mono<Void> filter(ServerWebExchange exchange, GatewayFilterChain chain) {
      ServerHttpRequest request = exchange.getRequest();
      HttpHeaders headers = request.getHeaders();
      String path = request.getURI().getPath();

      // ✅ Bypass filter untuk endpoint auth
      if (path.startsWith("/api/v1/auth/")) {
         return chain.filter(exchange);
      }

      // 1. Ambil Session ID dari Header
      if (!headers.containsKey("X-Session-Id")) {
         return onError(exchange, "Missing Session ID Header", HttpStatus.UNAUTHORIZED);
      }

      String sessionId = headers.getFirst("X-Session-Id");
      String redisKey = "AuthSession:" + sessionId;

      // 2. Ambil token dari Redis
      Map<Object, Object> authSession = redisTemplate.opsForHash().entries(redisKey);
      if (authSession.isEmpty() || !authSession.containsKey("token")) {
         return onError(exchange, "Invalid Session or Token Not Found", HttpStatus.UNAUTHORIZED);
      }

      String token = authSession.get("token").toString();
      String username = authSession.get("username").toString();
      String role = authSession.get("role").toString();

      // 3. Validasi token JWT
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

      // 4. Pastikan username 
      String jwtUsername = claims.getSubject();

      // ✅ Konversi manual agar tidak error
      Long userId = Long.parseLong(claims.get("userId").toString());

      if (!jwtUsername.equals(username)) {
         return onError(exchange, "Token does not match session username", HttpStatus.UNAUTHORIZED);
      }

      // ✅ 5. Cek otorisasi berdasarkan peran untuk rute tertentu
      if (path.startsWith("/api/v1/transactions/")) {
         if (!TRANSACTION_ALLOWED_ROLES.contains(role)) {
            log.warn("Access denied: User {} with role {} tried to access transaction service", username, role);
            return onError(exchange, "Access denied: Insufficient privileges for transaction service", HttpStatus.FORBIDDEN);
         }
         log.info("Role-based access granted for user {} with role {} to transaction service", username, role);
      }

      // ✅ 6. Teruskan informasi pengguna ke layanan lain
      ServerHttpRequest modifiedRequest = request.mutate()
            .header("X-Authenticated-User", username)
            .header("X-User-Id", String.valueOf(userId)) // Pastikan konversi eksplisit ke String
            .header("X-User-Role", role)
            .build();

      log.info("User {} authenticated successfully with role {}", username, role);
      return chain.filter(exchange.mutate().request(modifiedRequest).build());
   }

   private Mono<Void> onError(ServerWebExchange exchange, String err, HttpStatus status) {
      log.warn("Authentication error: {}", err);
      ServerHttpResponse response = exchange.getResponse();
      response.setStatusCode(status);
      return response.setComplete();
   }

   private Key getSignKey() {
      byte[] keyBytes = Decoders.BASE64.decode(SECRET_KEY);
      return Keys.hmacShaKeyFor(keyBytes);
   }

   @Override
   public int getOrder() {
      return -1; // Eksekusi lebih awal
   }
}
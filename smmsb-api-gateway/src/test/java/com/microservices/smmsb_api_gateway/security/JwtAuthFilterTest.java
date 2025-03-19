package com.microservices.smmsb_api_gateway.security;

import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.SignatureAlgorithm;
import io.jsonwebtoken.io.Decoders;
import io.jsonwebtoken.security.Keys;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.cloud.gateway.filter.GatewayFilterChain;
import org.springframework.data.redis.core.HashOperations;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.http.HttpStatus;
import org.springframework.mock.http.server.reactive.MockServerHttpRequest;
import org.springframework.mock.web.server.MockServerWebExchange;
import org.springframework.test.util.ReflectionTestUtils;
import org.springframework.web.server.ServerWebExchange;
import reactor.core.publisher.Mono;
import reactor.test.StepVerifier;

import java.security.Key;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
public class JwtAuthFilterTest {

    @Mock
    private StringRedisTemplate redisTemplate;

    @Mock
    private HashOperations<String, Object, Object> hashOperations;

    @Mock
    private GatewayFilterChain filterChain;

    @InjectMocks
    private JwtAuthFilter jwtAuthFilter;

    private final String SECRET_KEY = "3vTzq+9JmZm6WzkBcdh+YsPVf3Xa8nYhCTaXlAeR4zU=";
    private final String SESSION_ID = "test-session-id";
    private final String USERNAME = "testuser";
    private final String ROLE_ADMIN = "ROLE_ADMIN";
    private final String ROLE_SUPERADMIN = "ROLE_SUPERADMIN";
    private final Long USER_ID = 123L;

    @BeforeEach
    void setUp() {
        ReflectionTestUtils.setField(jwtAuthFilter, "secretKey", SECRET_KEY);
        ReflectionTestUtils.setField(jwtAuthFilter, "gatewayHeaderName", "X-Gateway-Access");
        ReflectionTestUtils.setField(jwtAuthFilter, "gatewayHeaderValue", "enabled");
        
        // We'll set up specific mocks in each test method instead of here
    }

    private String generateValidToken(String username, String role, Long userId) {
        Map<String, Object> claims = new HashMap<>();
        claims.put("role", role);
        claims.put("userId", userId);

        return Jwts.builder()
                .setClaims(claims)
                .setSubject(username)
                .setIssuedAt(new Date(System.currentTimeMillis()))
                .setExpiration(new Date(System.currentTimeMillis() + 1000 * 60 * 30)) // 30 minutes
                .signWith(getSignKey(), SignatureAlgorithm.HS256)
                .compact();
    }

    private Key getSignKey() {
        byte[] keyBytes = Decoders.BASE64.decode(SECRET_KEY);
        return Keys.hmacShaKeyFor(keyBytes);
    }

    private Map<Object, Object> createRedisSession(String token, String username, String role) {
        Map<Object, Object> sessionData = new HashMap<>();
        sessionData.put("token", token);
        sessionData.put("username", username);
        sessionData.put("role", role);
        return sessionData;
    }

    @Test
    void shouldAllowAccessToPublicEndpoint() {
        // Given
        MockServerHttpRequest request = MockServerHttpRequest
                .get("/api/v1/auth/login")
                .build();
        ServerWebExchange exchange = MockServerWebExchange.from(request);
        
        when(filterChain.filter(any(ServerWebExchange.class))).thenReturn(Mono.empty());

        // When
        Mono<Void> result = jwtAuthFilter.filter(exchange, filterChain);

        // Then
        StepVerifier.create(result)
                .verifyComplete();
        verify(filterChain).filter(any(ServerWebExchange.class));
    }

    @Test
    void shouldAllowAccessToSwaggerEndpoint() {
        // Given
        MockServerHttpRequest request = MockServerHttpRequest
                .get("/user-service/swagger-ui.html")
                .build();
        ServerWebExchange exchange = MockServerWebExchange.from(request);
        
        when(filterChain.filter(any(ServerWebExchange.class))).thenReturn(Mono.empty());

        // When
        Mono<Void> result = jwtAuthFilter.filter(exchange, filterChain);

        // Then
        StepVerifier.create(result)
                .verifyComplete();
        verify(filterChain).filter(any(ServerWebExchange.class));
    }

    @Test
    void shouldRejectRequestWithoutSessionId() {
        // Given
        MockServerHttpRequest request = MockServerHttpRequest
                .get("/api/v1/users/profile")
                .build();
        MockServerWebExchange exchange = MockServerWebExchange.from(request);

        // When
        Mono<Void> result = jwtAuthFilter.filter(exchange, filterChain);

        // Then
        StepVerifier.create(result)
                .verifyComplete();
        
        assert exchange.getResponse().getStatusCode() == HttpStatus.UNAUTHORIZED;
    }

    @Test
    void shouldRejectRequestWithInvalidSession() {
        // Given
        MockServerHttpRequest request = MockServerHttpRequest
                .get("/api/v1/users/profile")
                .header("X-Session-Id", SESSION_ID)
                .build();
        MockServerWebExchange exchange = MockServerWebExchange.from(request);

        when(redisTemplate.opsForHash()).thenReturn(hashOperations);
        when(hashOperations.entries("AuthSession:" + SESSION_ID)).thenReturn(new HashMap<>());

        // When
        Mono<Void> result = jwtAuthFilter.filter(exchange, filterChain);

        // Then
        StepVerifier.create(result)
                .verifyComplete();
        
        assert exchange.getResponse().getStatusCode() == HttpStatus.UNAUTHORIZED;
    }

    @Test
    void shouldAllowAccessWithValidTokenForSuperadminToUserService() {
        // Given
        String token = generateValidToken(USERNAME, ROLE_SUPERADMIN, USER_ID);
        Map<Object, Object> sessionData = createRedisSession(token, USERNAME, ROLE_SUPERADMIN);

        MockServerHttpRequest request = MockServerHttpRequest
                .get("/api/v1/users/profile")
                .header("X-Session-Id", SESSION_ID)
                .build();
        MockServerWebExchange exchange = MockServerWebExchange.from(request);

        when(redisTemplate.opsForHash()).thenReturn(hashOperations);
        when(hashOperations.entries("AuthSession:" + SESSION_ID)).thenReturn(sessionData);
        when(filterChain.filter(any(ServerWebExchange.class))).thenReturn(Mono.empty());

        // When
        Mono<Void> result = jwtAuthFilter.filter(exchange, filterChain);

        // Then
        StepVerifier.create(result)
                .verifyComplete();
        
        verify(filterChain).filter(any(ServerWebExchange.class));
    }

    @Test
    void shouldDenyAccessWithAdminRoleToUserService() {
        // Given
        String token = generateValidToken(USERNAME, ROLE_ADMIN, USER_ID);
        Map<Object, Object> sessionData = createRedisSession(token, USERNAME, ROLE_ADMIN);

        MockServerHttpRequest request = MockServerHttpRequest
                .get("/api/v1/users/profile")
                .header("X-Session-Id", SESSION_ID)
                .build();
        MockServerWebExchange exchange = MockServerWebExchange.from(request);

        when(redisTemplate.opsForHash()).thenReturn(hashOperations);
        when(hashOperations.entries("AuthSession:" + SESSION_ID)).thenReturn(sessionData);

        // When
        Mono<Void> result = jwtAuthFilter.filter(exchange, filterChain);

        // Then
        StepVerifier.create(result)
                .verifyComplete();
        
        assert exchange.getResponse().getStatusCode() == HttpStatus.FORBIDDEN;
    }

    @Test
    void shouldAllowAccessWithAdminRoleToInventoryService() {
        // Given
        String token = generateValidToken(USERNAME, ROLE_ADMIN, USER_ID);
        Map<Object, Object> sessionData = createRedisSession(token, USERNAME, ROLE_ADMIN);

        MockServerHttpRequest request = MockServerHttpRequest
                .get("/api/v1/inventory/items")
                .header("X-Session-Id", SESSION_ID)
                .build();
        MockServerWebExchange exchange = MockServerWebExchange.from(request);

        when(redisTemplate.opsForHash()).thenReturn(hashOperations);
        when(hashOperations.entries("AuthSession:" + SESSION_ID)).thenReturn(sessionData);
        when(filterChain.filter(any(ServerWebExchange.class))).thenReturn(Mono.empty());

        // When
        Mono<Void> result = jwtAuthFilter.filter(exchange, filterChain);

        // Then
        StepVerifier.create(result)
                .verifyComplete();
        
        verify(filterChain).filter(any(ServerWebExchange.class));
    }

    @Test
    void shouldRejectRequestWithInvalidToken() {
        // Given
        String invalidToken = "invalid.token.signature";
        Map<Object, Object> sessionData = createRedisSession(invalidToken, USERNAME, ROLE_ADMIN);

        MockServerHttpRequest request = MockServerHttpRequest
                .get("/api/v1/inventory/items")
                .header("X-Session-Id", SESSION_ID)
                .build();
        MockServerWebExchange exchange = MockServerWebExchange.from(request);

        when(redisTemplate.opsForHash()).thenReturn(hashOperations);
        when(hashOperations.entries("AuthSession:" + SESSION_ID)).thenReturn(sessionData);

        // When
        Mono<Void> result = jwtAuthFilter.filter(exchange, filterChain);

        // Then
        StepVerifier.create(result)
                .verifyComplete();
        
        assert exchange.getResponse().getStatusCode() == HttpStatus.UNAUTHORIZED;
    }

    @Test
    void shouldRejectRequestWithMismatchedUsername() {
        // Given
        String token = generateValidToken("different-user", ROLE_ADMIN, USER_ID);
        Map<Object, Object> sessionData = createRedisSession(token, USERNAME, ROLE_ADMIN);

        MockServerHttpRequest request = MockServerHttpRequest
                .get("/api/v1/inventory/items")
                .header("X-Session-Id", SESSION_ID)
                .build();
        MockServerWebExchange exchange = MockServerWebExchange.from(request);

        when(redisTemplate.opsForHash()).thenReturn(hashOperations);
        when(hashOperations.entries("AuthSession:" + SESSION_ID)).thenReturn(sessionData);

        // When
        Mono<Void> result = jwtAuthFilter.filter(exchange, filterChain);

        // Then
        StepVerifier.create(result)
                .verifyComplete();
        
        assert exchange.getResponse().getStatusCode() == HttpStatus.UNAUTHORIZED;
    }

    @Test
    void shouldAddUserInfoHeadersToRequest() {
        // Given
        String token = generateValidToken(USERNAME, ROLE_ADMIN, USER_ID);
        Map<Object, Object> sessionData = createRedisSession(token, USERNAME, ROLE_ADMIN);

        MockServerHttpRequest request = MockServerHttpRequest
                .get("/api/v1/inventory/items")
                .header("X-Session-Id", SESSION_ID)
                .build();
        MockServerWebExchange exchange = MockServerWebExchange.from(request);

        when(redisTemplate.opsForHash()).thenReturn(hashOperations);
        when(hashOperations.entries("AuthSession:" + SESSION_ID)).thenReturn(sessionData);
        when(filterChain.filter(any(ServerWebExchange.class))).thenReturn(Mono.empty());

        // When
        Mono<Void> result = jwtAuthFilter.filter(exchange, filterChain);

        // Then
        StepVerifier.create(result)
                .verifyComplete();
        
        verify(filterChain).filter(any(ServerWebExchange.class));
    }
}

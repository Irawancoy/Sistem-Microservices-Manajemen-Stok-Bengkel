package com.microservices.smmsb_user_service.service.impl;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

import java.util.Optional;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpStatus;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;

import com.microservices.smmsb_user_service.dto.request.LoginRequest;
import com.microservices.smmsb_user_service.dto.response.ApiDataResponseBuilder;
import com.microservices.smmsb_user_service.dto.response.LoginResponse;
import com.microservices.smmsb_user_service.exception.ResourceNotFoundException;
import com.microservices.smmsb_user_service.model.AuthSession;
import com.microservices.smmsb_user_service.model.User;
import com.microservices.smmsb_user_service.repository.AuthSessionRepository;
import com.microservices.smmsb_user_service.repository.UserRepository;
import com.microservices.smmsb_user_service.security.JwtUtil;
import com.microservices.smmsb_user_service.utils.MessageUtils;

@ExtendWith(MockitoExtension.class)
public class AuthServiceImplTest {

   @Mock
   private AuthenticationManager authenticationManager;

   @Mock
   private JwtUtil jwtUtil;

   @Mock
   private UserRepository userRepository;

   @Mock
   private MessageUtils messageUtils;

   @Mock
   private AuthSessionRepository authSessionRepository;

   @Mock
   private Authentication authentication;

   @InjectMocks
   private AuthServiceImpl authService;

   private LoginRequest loginRequest;
   private User user;
   private String token;

   @BeforeEach
   void setUp() {
      loginRequest = new LoginRequest();
      loginRequest.setUsername("testuser");
      loginRequest.setPassword("password");

      user = new User();
      user.setId(1L);
      user.setUsername("testuser");
      user.setRole("ROLE_SUPERADMIN");

      token = "test.jwt.token";
   }

   @Test
   void login_Success() {
      // Arrange
      when(authenticationManager.authenticate(any(UsernamePasswordAuthenticationToken.class)))
            .thenReturn(authentication);
      when(userRepository.findByUsername("testuser")).thenReturn(Optional.of(user));
      when(jwtUtil.generateToken(authentication, 1L)).thenReturn(token);
      when(messageUtils.getMessage("success.login")).thenReturn("Login successful");
      when(authSessionRepository.save(any(AuthSession.class))).thenAnswer(invocation -> {
         AuthSession session = invocation.getArgument(0);
         session.setSessionId("test-session-id");
         return session;
      });

      // Act
      ApiDataResponseBuilder result = authService.login(loginRequest);

      // Assert
      assertNotNull(result);
      assertEquals(HttpStatus.OK, result.getStatus());
      assertEquals(HttpStatus.OK.value(), result.getStatusCode());
      assertEquals("Login successful", result.getMessage());

      LoginResponse loginResponse = (LoginResponse) result.getData();
      assertNotNull(loginResponse);
      assertEquals(token, loginResponse.getToken());
      assertEquals("testuser", loginResponse.getUsername());
      assertEquals("ROLE_SUPERADMIN", loginResponse.getRole());
      assertEquals("test-session-id", loginResponse.getSessionId());

      verify(authSessionRepository).save(any(AuthSession.class));
   }

   @Test
   void login_InvalidCredentials() {
      // Arrange
      when(authenticationManager.authenticate(any(UsernamePasswordAuthenticationToken.class)))
            .thenThrow(new BadCredentialsException("Invalid credentials"));
      when(messageUtils.getMessage("error.login.invalid.credentials"))
            .thenReturn("Invalid username or password");

      // Act & Assert
      BadCredentialsException exception = assertThrows(BadCredentialsException.class, () -> {
         authService.login(loginRequest);
      });

      assertEquals("Invalid username or password", exception.getMessage());
      verify(userRepository, never()).findByUsername(anyString());
      verify(jwtUtil, never()).generateToken(any(), anyLong());
      verify(authSessionRepository, never()).save(any());
   }

   @Test
   void login_UserNotFound() {
      // Arrange
      when(authenticationManager.authenticate(any(UsernamePasswordAuthenticationToken.class)))
            .thenReturn(authentication);
      when(userRepository.findByUsername("testuser")).thenReturn(Optional.empty());
      when(messageUtils.getMessage("error.login.username.not.found"))
            .thenReturn("User not found");

      // Act & Assert
      ResourceNotFoundException exception = assertThrows(ResourceNotFoundException.class, () -> {
         authService.login(loginRequest);
      });

      assertEquals("User not found", exception.getMessage());
      verify(jwtUtil, never()).generateToken(any(), anyLong());
      verify(authSessionRepository, never()).save(any());
   }
}

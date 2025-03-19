package com.microservices.smmsb_user_service.service.impl;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
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
import org.springframework.security.core.userdetails.UsernameNotFoundException;

import com.microservices.smmsb_user_service.dto.request.LoginRequest;
import com.microservices.smmsb_user_service.dto.response.ApiDataResponseBuilder;
import com.microservices.smmsb_user_service.dto.response.LoginResponse;
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
   private AuthSession authSession;
   private String jwtToken;

   @BeforeEach
   void setUp() {
      // Setup test data
      loginRequest = new LoginRequest();
      loginRequest.setUsername("testuser");
      loginRequest.setPassword("password123");

      user = new User();
      user.setId(1L);
      user.setUsername("testuser");
      user.setEmail("test@example.com");
      user.setPasswordHash("hashedpassword");
      user.setRole("ROLE_ADMIN"); // ROLE_ADMIN or ROLE_SUPERADMIN

      jwtToken = "test.jwt.token";

      authSession = new AuthSession("testuser", jwtToken, "ROLE_ADMIN");
      authSession.setSessionId("test-session-id");
   }

   @Test
   void login_Success() {
      // Arrange
      when(authenticationManager.authenticate(any(UsernamePasswordAuthenticationToken.class)))
            .thenReturn(authentication);
      when(authentication.isAuthenticated()).thenReturn(true);
      when(userRepository.findByUsername("testuser")).thenReturn(Optional.of(user));
      when(jwtUtil.generateToken(authentication, 1L)).thenReturn(jwtToken);

      // Mock the AuthSession creation and save
      when(authSessionRepository.save(any(AuthSession.class))).thenAnswer(invocation -> {
         AuthSession session = invocation.getArgument(0);
         // Set the session ID to our expected value
         session.setSessionId("test-session-id");
         return session;
      });

      when(messageUtils.getMessage("success.login")).thenReturn("Login successful");

      // Act
      ApiDataResponseBuilder response = authService.login(loginRequest);

      // Assert
      assertNotNull(response);
      assertEquals(HttpStatus.OK, response.getStatus());
      assertEquals(HttpStatus.OK.value(), response.getStatusCode());

      // Verify the response data
      LoginResponse loginResponse = (LoginResponse) response.getData();
      assertEquals(jwtToken, loginResponse.getToken());
      assertEquals("testuser", loginResponse.getUsername());
      assertEquals("ROLE_ADMIN", loginResponse.getRole());
      assertEquals("test-session-id", loginResponse.getSessionId());

      // Verify interactions
      verify(authenticationManager).authenticate(any(UsernamePasswordAuthenticationToken.class));
      verify(userRepository).findByUsername("testuser");
      verify(jwtUtil).generateToken(authentication, 1L);
      verify(authSessionRepository).save(any(AuthSession.class));
   }

   @Test
   void login_AuthenticationFailed() {
      // Arrange
      when(authenticationManager.authenticate(any(UsernamePasswordAuthenticationToken.class)))
            .thenReturn(authentication);
      when(authentication.isAuthenticated()).thenReturn(false);
      when(messageUtils.getMessage("error.login.invalid.request")).thenReturn("Invalid login credentials");

      // Act & Assert
      Exception exception = assertThrows(UsernameNotFoundException.class, () -> {
         authService.login(loginRequest);
      });

      assertEquals("Invalid login credentials", exception.getMessage());

      // Verify interactions
      verify(authenticationManager).authenticate(any(UsernamePasswordAuthenticationToken.class));
      verify(userRepository, never()).findByUsername(anyString());
      verify(jwtUtil, never()).generateToken(any(), anyLong());
      verify(authSessionRepository, never()).save(any(AuthSession.class));
   }

   @Test
   void login_BadCredentials() {
      // Arrange
      when(authenticationManager.authenticate(any(UsernamePasswordAuthenticationToken.class)))
            .thenThrow(new BadCredentialsException("Bad credentials"));
      // when(messageUtils.getMessage("error.login.invalid.request")).thenReturn("Invalid
      // login credentials");

      // Act & Assert
      Exception exception = assertThrows(BadCredentialsException.class, () -> {
         authService.login(loginRequest);
      });

      assertEquals("Bad credentials", exception.getMessage());

      // Verify interactions
      verify(authenticationManager).authenticate(any(UsernamePasswordAuthenticationToken.class));
      verify(userRepository, never()).findByUsername(anyString());
      verify(jwtUtil, never()).generateToken(any(), anyLong());
      verify(authSessionRepository, never()).save(any(AuthSession.class));
   }

   @Test
   void login_UserNotFound() {
      // Arrange
      when(authenticationManager.authenticate(any(UsernamePasswordAuthenticationToken.class)))
            .thenReturn(authentication);
      when(authentication.isAuthenticated()).thenReturn(true);
      when(userRepository.findByUsername("testuser")).thenReturn(Optional.empty());
      when(messageUtils.getMessage("error.login.invalid.request")).thenReturn("Invalid login credentials");

      // Act & Assert
      Exception exception = assertThrows(UsernameNotFoundException.class, () -> {
         authService.login(loginRequest);
      });

      assertEquals("Invalid login credentials", exception.getMessage());

      // Verify interactions
      verify(authenticationManager).authenticate(any(UsernamePasswordAuthenticationToken.class));
      verify(userRepository).findByUsername("testuser");
      verify(jwtUtil, never()).generateToken(any(), anyLong());
      verify(authSessionRepository, never()).save(any(AuthSession.class));
   }

   @Test
   void login_TokenGenerationFails() {
      // Arrange
      when(authenticationManager.authenticate(any(UsernamePasswordAuthenticationToken.class)))
            .thenReturn(authentication);
      when(authentication.isAuthenticated()).thenReturn(true);
      when(userRepository.findByUsername("testuser")).thenReturn(Optional.of(user));
      when(jwtUtil.generateToken(authentication, 1L)).thenThrow(new RuntimeException("Token generation failed"));

      // Act & Assert
      Exception exception = assertThrows(RuntimeException.class, () -> {
         authService.login(loginRequest);
      });

      assertEquals("Token generation failed", exception.getMessage());

      // Verify interactions
      verify(authenticationManager).authenticate(any(UsernamePasswordAuthenticationToken.class));
      verify(userRepository).findByUsername("testuser");
      verify(jwtUtil).generateToken(authentication, 1L);
      verify(authSessionRepository, never()).save(any(AuthSession.class));
   }

   @Test
   void login_SessionSaveFails() {
      // Arrange
      when(authenticationManager.authenticate(any(UsernamePasswordAuthenticationToken.class)))
            .thenReturn(authentication);
      when(authentication.isAuthenticated()).thenReturn(true);
      when(userRepository.findByUsername("testuser")).thenReturn(Optional.of(user));
      when(jwtUtil.generateToken(authentication, 1L)).thenReturn(jwtToken);
      when(authSessionRepository.save(any(AuthSession.class))).thenThrow(new RuntimeException("Session save failed"));

      // Act & Assert
      Exception exception = assertThrows(RuntimeException.class, () -> {
         authService.login(loginRequest);
      });

      assertEquals("Session save failed", exception.getMessage());

      // Verify interactions
      verify(authenticationManager).authenticate(any(UsernamePasswordAuthenticationToken.class));
      verify(userRepository).findByUsername("testuser");
      verify(jwtUtil).generateToken(authentication, 1L);
      verify(authSessionRepository).save(any(AuthSession.class));
   }
}

package com.microservices.smmsb_user_service.service.impl;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

import java.util.Optional;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UsernameNotFoundException;

import com.microservices.smmsb_user_service.model.User;
import com.microservices.smmsb_user_service.repository.UserRepository;
import com.microservices.smmsb_user_service.security.CustomUserDetails;

@ExtendWith(MockitoExtension.class)
public class UserDetailsServiceImplTest {

   @Mock
   private UserRepository userRepository;

   @InjectMocks
   private UserDetailsServiceImpl userDetailsService;

   private User testUser;

   @BeforeEach
   void setUp() {
      testUser = new User();
      testUser.setId(1L);
      testUser.setUsername("testuser");
      testUser.setEmail("test@example.com");
      testUser.setPasswordHash("hashedpassword");
      testUser.setRole("ROLE_SUPERADMIN");
   }

   @Test
   void loadUserByUsername_Success() {
      // Arrange
      when(userRepository.findByUsername("testuser")).thenReturn(Optional.of(testUser));

      // Act
      UserDetails userDetails = userDetailsService.loadUserByUsername("testuser");

      // Assert
      assertNotNull(userDetails);
      assertTrue(userDetails instanceof CustomUserDetails);
      assertEquals("testuser", userDetails.getUsername());
      assertEquals("hashedpassword", userDetails.getPassword());
      assertTrue(userDetails.isEnabled());
      assertTrue(userDetails.isAccountNonExpired());
      assertTrue(userDetails.isCredentialsNonExpired());
      assertTrue(userDetails.isAccountNonLocked());

      // Verify that the repository method was called
      verify(userRepository).findByUsername("testuser");
   }

   @Test
   void loadUserByUsername_UserNotFound() {
      // Arrange
      when(userRepository.findByUsername("nonexistentuser")).thenReturn(Optional.empty());

      // Act & Assert
      UsernameNotFoundException exception = assertThrows(UsernameNotFoundException.class, () -> {
         userDetailsService.loadUserByUsername("nonexistentuser");
      });

      assertEquals("User not found nonexistentuser", exception.getMessage());
      verify(userRepository).findByUsername("nonexistentuser");
   }
}

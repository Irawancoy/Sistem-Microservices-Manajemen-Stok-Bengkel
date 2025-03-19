package com.microservices.smmsb_user_service.service.impl;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

import java.util.Arrays;
import java.util.List;
import java.util.Optional;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.modelmapper.ModelMapper;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.http.HttpStatus;
import org.springframework.security.crypto.password.PasswordEncoder;

import com.microservices.smmsb_user_service.dto.UserDto;
import com.microservices.smmsb_user_service.dto.request.CreateUserRequest;
import com.microservices.smmsb_user_service.dto.request.UpdateUserRequest;
import com.microservices.smmsb_user_service.dto.response.ApiDataResponseBuilder;
import com.microservices.smmsb_user_service.dto.response.ListResponse;
import com.microservices.smmsb_user_service.dto.response.MessageResponse;
import com.microservices.smmsb_user_service.exception.DuplicateResourceException;
import com.microservices.smmsb_user_service.exception.ResourceNotFoundException;
import com.microservices.smmsb_user_service.model.User;
import com.microservices.smmsb_user_service.repository.UserRepository;
import com.microservices.smmsb_user_service.utils.MessageUtils;

@ExtendWith(MockitoExtension.class)
public class UserServiceImplTest {

   @Mock
   private UserRepository userRepository;

   @Mock
   private PasswordEncoder passwordEncoder;

   @Mock
   private MessageUtils messageUtils;

   @Mock
   private ModelMapper modelMapper;

   @InjectMocks
   private UserServiceImpl userService;

   private User user;
   private UserDto userDto;
   private CreateUserRequest createUserRequest;
   private UpdateUserRequest updateUserRequest;
   private Pageable pageable;

   @BeforeEach
   void setUp() {
      // Setup test data
      user = new User();
      user.setId(1L);
      user.setUsername("testuser");
      user.setEmail("test@example.com");
      user.setPasswordHash("hashedpassword");
      user.setRole("ROLE_SUPERADMIN");

      userDto = new UserDto();
      userDto.setId(1L);
      userDto.setUsername("testuser");
      userDto.setEmail("test@example.com");
      userDto.setRole("ROLE_SUPERADMIN");

      createUserRequest = new CreateUserRequest();
      createUserRequest.setUsername("newuser");
      createUserRequest.setEmail("new@example.com");
      createUserRequest.setPassword("password123");
      createUserRequest.setRole("ROLE_SUPERADMIN");

      updateUserRequest = new UpdateUserRequest();
      updateUserRequest.setUsername("updateduser");
      updateUserRequest.setEmail("updated@example.com");
      updateUserRequest.setPassword("newpassword");
      updateUserRequest.setRole("ADMIN");

      pageable = PageRequest.of(0, 10, Sort.by("id").ascending());
   }

   @Test
   void createUser_Success() {
      // Arrange
      when(userRepository.existsByUsername(createUserRequest.getUsername())).thenReturn(false);
      when(userRepository.existsByEmail(createUserRequest.getEmail())).thenReturn(false);
      when(modelMapper.map(createUserRequest, User.class)).thenReturn(user);
      when(passwordEncoder.encode(createUserRequest.getPassword())).thenReturn("hashedpassword");
      when(messageUtils.getMessage("success.user.created", createUserRequest.getUsername()))
            .thenReturn("User created successfully");

      // Act
      MessageResponse response = userService.createUser(createUserRequest);

      // Assert
      assertNotNull(response);
      assertEquals(HttpStatus.CREATED.value(), response.getStatusCode());
      assertEquals("User created successfully", response.getMessage());

      verify(userRepository).existsByUsername(createUserRequest.getUsername());
      verify(userRepository).existsByEmail(createUserRequest.getEmail());
      verify(passwordEncoder).encode(createUserRequest.getPassword());
      verify(userRepository).save(user);
   }

   @Test
   void createUser_UsernameAlreadyExists() {
      // Arrange
      when(userRepository.existsByUsername(createUserRequest.getUsername())).thenReturn(true);
      when(messageUtils.getMessage("error.user.username.already.exists", createUserRequest.getUsername()))
            .thenReturn("Username already exists");

      // Act & Assert
      DuplicateResourceException exception = assertThrows(DuplicateResourceException.class, () -> {
         userService.createUser(createUserRequest);
      });

      assertEquals("Username already exists", exception.getMessage());
      verify(userRepository).existsByUsername(createUserRequest.getUsername());
      verify(userRepository, never()).save(any(User.class));
   }

   @Test
   void createUser_EmailAlreadyExists() {
      // Arrange
      when(userRepository.existsByUsername(createUserRequest.getUsername())).thenReturn(false);
      when(userRepository.existsByEmail(createUserRequest.getEmail())).thenReturn(true);
      when(messageUtils.getMessage("error.user.email.already.exists", createUserRequest.getEmail()))
            .thenReturn("Email already exists");

      // Act & Assert
      DuplicateResourceException exception = assertThrows(DuplicateResourceException.class, () -> {
         userService.createUser(createUserRequest);
      });

      assertEquals("Email already exists", exception.getMessage());
      verify(userRepository).existsByUsername(createUserRequest.getUsername());
      verify(userRepository).existsByEmail(createUserRequest.getEmail());
      verify(userRepository, never()).save(any(User.class));
   }

   @Test
   void updateUser_Success() {
      // Arrange
      User userToUpdate = new User();
      userToUpdate.setId(1L);
      userToUpdate.setUsername("testuser");
      userToUpdate.setEmail("test@example.com");
      userToUpdate.setPasswordHash("hashedpassword");
      userToUpdate.setRole("ROLE_SUPERADMIN");

      when(userRepository.findById(1L)).thenReturn(Optional.of(userToUpdate));
      when(passwordEncoder.encode(updateUserRequest.getPassword())).thenReturn("newhashpassword");

      // This is the key change - we need to stub with the updated username
      when(messageUtils.getMessage("success.user.updated", "updateduser"))
            .thenReturn("User updated successfully");

      // Act
      MessageResponse response = userService.updateUser(1L, updateUserRequest);

      // Assert
      assertNotNull(response);
      assertEquals(HttpStatus.OK.value(), response.getStatusCode());
      assertEquals("User updated successfully", response.getMessage());

      verify(userRepository).findById(1L);
      verify(passwordEncoder).encode(updateUserRequest.getPassword());

      // Verify user properties were updated
      assertEquals(updateUserRequest.getUsername(), userToUpdate.getUsername());
      assertEquals(updateUserRequest.getEmail(), userToUpdate.getEmail());
      assertEquals(updateUserRequest.getRole(), userToUpdate.getRole());
      assertEquals("newhashpassword", userToUpdate.getPasswordHash());
   }

   @Test
   void updateUser_UserNotFound() {
      // Arrange
      when(userRepository.findById(1L)).thenReturn(Optional.empty());
      when(messageUtils.getMessage("error.user.not.found"))
            .thenReturn("User not found");

      // Act & Assert
      ResourceNotFoundException exception = assertThrows(ResourceNotFoundException.class, () -> {
         userService.updateUser(1L, updateUserRequest);
      });

      assertEquals("User not found", exception.getMessage());
      verify(userRepository).findById(1L);
   }

   @Test
   void updateUser_UsernameAlreadyExists() {
      // Arrange
      when(userRepository.findById(1L)).thenReturn(Optional.of(user));
      when(userRepository.existsByUsername(updateUserRequest.getUsername())).thenReturn(true);
      when(messageUtils.getMessage("error.user.username.already.exists", updateUserRequest.getUsername()))
            .thenReturn("Username already exists");

      // Act & Assert
      DuplicateResourceException exception = assertThrows(DuplicateResourceException.class, () -> {
         userService.updateUser(1L, updateUserRequest);
      });

      assertEquals("Username already exists", exception.getMessage());
      verify(userRepository).findById(1L);
      verify(userRepository).existsByUsername(updateUserRequest.getUsername());
   }

   @Test
   void updateUser_EmailAlreadyExists() {
      // Arrange
      when(userRepository.findById(1L)).thenReturn(Optional.of(user));
      when(userRepository.existsByUsername(updateUserRequest.getUsername())).thenReturn(false);
      when(userRepository.existsByEmail(updateUserRequest.getEmail())).thenReturn(true);
      when(messageUtils.getMessage("error.user.email.already.exists", updateUserRequest.getEmail()))
            .thenReturn("Email already exists");

      // Act & Assert
      DuplicateResourceException exception = assertThrows(DuplicateResourceException.class, () -> {
         userService.updateUser(1L, updateUserRequest);
      });

      assertEquals("Email already exists", exception.getMessage());
      verify(userRepository).findById(1L);
      verify(userRepository).existsByUsername(updateUserRequest.getUsername());
      verify(userRepository).existsByEmail(updateUserRequest.getEmail());
   }

   @Test
   void updateUser_PartialUpdate() {
      // Arrange
      UpdateUserRequest partialRequest = new UpdateUserRequest();
      partialRequest.setUsername("updateduser"); // Only update username

      when(userRepository.findById(1L)).thenReturn(Optional.of(user));
      when(userRepository.existsByUsername(partialRequest.getUsername())).thenReturn(false);
      when(messageUtils.getMessage("success.user.updated", "updateduser"))
            .thenReturn("User updated successfully");

      // Act
      MessageResponse response = userService.updateUser(1L, partialRequest);

      // Assert
      assertNotNull(response);
      assertEquals(HttpStatus.OK.value(), response.getStatusCode());
      assertEquals("User updated successfully", response.getMessage());

      verify(userRepository).findById(1L);

      // Verify only username was updated
      assertEquals("updateduser", user.getUsername());
      assertEquals("test@example.com", user.getEmail()); // Unchanged
      assertEquals("ROLE_SUPERADMIN", user.getRole()); // Unchanged
      assertEquals("hashedpassword", user.getPasswordHash()); // Unchanged
   }

   @Test
   void deleteUser_Success() {
      // Arrange
      when(userRepository.existsById(1L)).thenReturn(true);
      when(messageUtils.getMessage("success.user.deleted", 1L))
            .thenReturn("User deleted successfully");

      // Act
      MessageResponse response = userService.deleteUser(1L);

      // Assert
      assertNotNull(response);
      assertEquals(HttpStatus.OK.value(), response.getStatusCode());
      assertEquals("User deleted successfully", response.getMessage());

      verify(userRepository).existsById(1L);
      verify(userRepository).deleteById(1L);
   }

   @Test
   void deleteUser_UserNotFound() {
      // Arrange
      when(userRepository.existsById(1L)).thenReturn(false);
      when(messageUtils.getMessage("error.user.not.found"))
            .thenReturn("User not found");

      // Act & Assert
      ResourceNotFoundException exception = assertThrows(ResourceNotFoundException.class, () -> {
         userService.deleteUser(1L);
      });

      assertEquals("User not found", exception.getMessage());
      verify(userRepository).existsById(1L);
      verify(userRepository, never()).deleteById(anyLong());
   }

   @SuppressWarnings("unchecked")
   @Test
   void getAllUsers_Success() {
      // Arrange
      List<User> userList = Arrays.asList(user);
      Page<User> userPage = new PageImpl<>(userList, pageable, userList.size());

      when(userRepository.findAll(any(Specification.class), eq(pageable))).thenReturn(userPage);
      when(modelMapper.map(user, UserDto.class)).thenReturn(userDto);
      when(messageUtils.getMessage("success.user.retrieved"))
            .thenReturn("Users retrieved successfully");

      // Act
      ListResponse<UserDto> response = userService.getAllUsers(pageable, null, null, null);

      // Assert
      assertNotNull(response);
      assertEquals(HttpStatus.OK.value(), response.getStatusCode());
      assertEquals("Users retrieved successfully", response.getMessage());
      assertEquals(1, response.getData().size());
      assertEquals(userDto, response.getData().get(0));

      verify(userRepository).findAll(any(Specification.class), eq(pageable));
   }

   @SuppressWarnings("unchecked")
   @Test
   void getAllUsers_WithFilters() {
      // Arrange
      List<User> userList = Arrays.asList(user);
      Page<User> userPage = new PageImpl<>(userList, pageable, userList.size());

      when(userRepository.findAll(any(Specification.class), eq(pageable))).thenReturn(userPage);
      when(modelMapper.map(user, UserDto.class)).thenReturn(userDto);
      when(messageUtils.getMessage("success.user.retrieved"))
            .thenReturn("Users retrieved successfully");

      // Act
      ListResponse<UserDto> response = userService.getAllUsers(pageable, "testuser", "test@example.com", "ROLE_SUPERADMIN");

      // Assert
      assertNotNull(response);
      assertEquals(HttpStatus.OK.value(), response.getStatusCode());
      assertEquals("Users retrieved successfully", response.getMessage());
      assertEquals(1, response.getData().size());

      verify(userRepository).findAll(any(Specification.class), eq(pageable));
   }

   @Test
   void getUserById_Success() {
      // Arrange
      when(userRepository.findById(1L)).thenReturn(Optional.of(user));
      when(modelMapper.map(user, UserDto.class)).thenReturn(userDto);
      when(messageUtils.getMessage("success.user.retrieved"))
            .thenReturn("User retrieved successfully");

      // Act
      ApiDataResponseBuilder response = userService.getUserById(1L);

      // Assert
      assertNotNull(response);
      assertEquals(HttpStatus.OK, response.getStatus());
      assertEquals(HttpStatus.OK.value(), response.getStatusCode());
      assertEquals("User retrieved successfully", response.getMessage());
      assertEquals(userDto, response.getData());

      verify(userRepository).findById(1L);
   }

   @Test
   void getUserById_UserNotFound() {
      // Arrange
      when(userRepository.findById(1L)).thenReturn(Optional.empty());
      when(messageUtils.getMessage("error.user.not.found"))
            .thenReturn("User not found");

      // Act & Assert
      ResourceNotFoundException exception = assertThrows(ResourceNotFoundException.class, () -> {
         userService.getUserById(1L);
      });

      assertEquals("User not found", exception.getMessage());
      verify(userRepository).findById(1L);
   }
}

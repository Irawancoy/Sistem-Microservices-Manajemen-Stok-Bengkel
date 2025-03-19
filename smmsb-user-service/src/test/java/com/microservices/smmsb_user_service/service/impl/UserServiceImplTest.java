package com.microservices.smmsb_user_service.service.impl;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

import java.util.Arrays;
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
import org.springframework.data.jpa.domain.Specification;
import org.springframework.http.HttpStatus;
import org.springframework.security.crypto.password.PasswordEncoder;

import com.microservices.smmsb_user_service.dto.UserDto;
import com.microservices.smmsb_user_service.dto.request.CreateUserRequest;
import com.microservices.smmsb_user_service.dto.request.UpdateUserRequest;
import com.microservices.smmsb_user_service.dto.response.ApiDataResponseBuilder;
import com.microservices.smmsb_user_service.dto.response.ListResponse;
import com.microservices.smmsb_user_service.dto.response.MessageResponse;
import com.microservices.smmsb_user_service.model.User;
import com.microservices.smmsb_user_service.repository.UserRepository;
import com.microservices.smmsb_user_service.utils.MessageUtils;

import jakarta.servlet.http.HttpServletRequest;

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

    @Mock
    private HttpServletRequest httpServletRequest;

    @InjectMocks
    private UserServiceImpl userService;

    private User user;
    private UserDto userDto;
    private CreateUserRequest createUserRequest;
    private UpdateUserRequest updateUserRequest;

    @BeforeEach
    void setUp() {
        // Setup test data
        user = new User();
        user.setId(1L);
        user.setUsername("testuser");
        user.setEmail("test@example.com");
        user.setPasswordHash("hashedpassword");
        user.setRole("USER");

        userDto = new UserDto();
        userDto.setId(1L);
        userDto.setUsername("testuser");
        userDto.setEmail("test@example.com");
        userDto.setRole("USER");

        createUserRequest = new CreateUserRequest();
        createUserRequest.setUsername("newuser");
        createUserRequest.setEmail("new@example.com");
        createUserRequest.setPassword("password123");
        createUserRequest.setRole("USER");

        updateUserRequest = new UpdateUserRequest();
        updateUserRequest.setUsername("updateduser");
        updateUserRequest.setEmail("updated@example.com");
        updateUserRequest.setPassword("newpassword123");
        updateUserRequest.setRole("ADMIN");
    }

    @Test
    void createUser_Success() {
        // Arrange
        when(userRepository.existsByUsername(createUserRequest.getUsername())).thenReturn(false);
        when(userRepository.existsByEmail(createUserRequest.getEmail())).thenReturn(false);
        when(modelMapper.map(createUserRequest, User.class)).thenReturn(user);
        when(passwordEncoder.encode(createUserRequest.getPassword())).thenReturn("encodedPassword");
        when(messageUtils.getMessage("success.user.created", createUserRequest.getUsername()))
                .thenReturn("User created successfully: newuser");

        // Act
        MessageResponse response = userService.createUser(createUserRequest, httpServletRequest);

        // Assert
        assertNotNull(response);
        assertEquals(HttpStatus.CREATED.value(), response.getStatusCode());
        assertEquals(HttpStatus.CREATED.name(), response.getStatus());
        
        // Verify interactions
        verify(userRepository).save(user);
        verify(passwordEncoder).encode(createUserRequest.getPassword());
    }

    @Test
    void createUser_UsernameExists() {
        // Arrange
        when(userRepository.existsByUsername(createUserRequest.getUsername())).thenReturn(true);
        when(messageUtils.getMessage("error.user.username.already.exists", createUserRequest.getUsername()))
                .thenReturn("Username already exists: newuser");

        // Act
        MessageResponse response = userService.createUser(createUserRequest, httpServletRequest);

        // Assert
        assertNotNull(response);
        assertEquals(HttpStatus.BAD_REQUEST.value(), response.getStatusCode());
        assertEquals(HttpStatus.BAD_REQUEST.name(), response.getStatus());
        
        // Verify no interactions with save
        verify(userRepository, never()).save(any(User.class));
    }

    @Test
    void createUser_EmailExists() {
        // Arrange
        when(userRepository.existsByUsername(createUserRequest.getUsername())).thenReturn(false);
        when(userRepository.existsByEmail(createUserRequest.getEmail())).thenReturn(true);
        when(messageUtils.getMessage("error.user.email.already.exists", createUserRequest.getEmail()))
                .thenReturn("Email already exists: new@example.com");

        // Act
        MessageResponse response = userService.createUser(createUserRequest, httpServletRequest);

        // Assert
        assertNotNull(response);
        assertEquals(HttpStatus.BAD_REQUEST.value(), response.getStatusCode());
        assertEquals(HttpStatus.BAD_REQUEST.name(), response.getStatus());
        
        // Verify no interactions with save
        verify(userRepository, never()).save(any(User.class));
    }

    @Test
    void updateUser_Success() {
        // Arrange
        when(userRepository.findById(1L)).thenReturn(Optional.of(user));
        when(userRepository.existsByUsername(updateUserRequest.getUsername())).thenReturn(false);
        when(userRepository.existsByEmail(updateUserRequest.getEmail())).thenReturn(false);
        when(passwordEncoder.encode(updateUserRequest.getPassword())).thenReturn("newEncodedPassword");
        when(messageUtils.getMessage("success.user.updated", updateUserRequest.getUsername()))
                .thenReturn("User updated successfully: updateduser");

        // Act
        MessageResponse response = userService.updateUser(1L, updateUserRequest);

        // Assert
        assertNotNull(response);
        assertEquals(HttpStatus.OK.value(), response.getStatusCode());
        assertEquals(HttpStatus.OK.name(), response.getStatus());
        
        // Verify user was updated
        assertEquals(updateUserRequest.getUsername(), user.getUsername());
        assertEquals(updateUserRequest.getEmail(), user.getEmail());
        assertEquals(updateUserRequest.getRole(), user.getRole());
        verify(passwordEncoder).encode(updateUserRequest.getPassword());
    }

    @Test
    void updateUser_UserNotFound() {
        // Arrange
        when(userRepository.findById(999L)).thenReturn(Optional.empty());
        when(messageUtils.getMessage("error.user.not.found", 999L))
                .thenReturn("User not found with ID: 999");

        // Act & Assert
        assertThrows(RuntimeException.class, () -> {
            userService.updateUser(999L, updateUserRequest);
        });
    }

    @Test
    void updateUser_UsernameExists() {
        // Arrange
        when(userRepository.findById(1L)).thenReturn(Optional.of(user));
        when(userRepository.existsByUsername(updateUserRequest.getUsername())).thenReturn(true);
        when(messageUtils.getMessage("error.user.username.already.exists", updateUserRequest.getUsername()))
                .thenReturn("Username already exists: updateduser");

        // Act
        MessageResponse response = userService.updateUser(1L, updateUserRequest);

        // Assert
        assertNotNull(response);
        assertEquals(HttpStatus.BAD_REQUEST.value(), response.getStatusCode());
        assertEquals(HttpStatus.BAD_REQUEST.name(), response.getStatus());
        
        // Verify user was not updated
        assertNotEquals(updateUserRequest.getUsername(), user.getUsername());
    }

    @Test
    void updateUser_EmailExists() {
        // Arrange
        when(userRepository.findById(1L)).thenReturn(Optional.of(user));
        when(userRepository.existsByUsername(updateUserRequest.getUsername())).thenReturn(false);
        when(userRepository.existsByEmail(updateUserRequest.getEmail())).thenReturn(true);
        when(messageUtils.getMessage("error.user.email.already.exists", updateUserRequest.getEmail()))
                .thenReturn("Email already exists: updated@example.com");

        // Act
        MessageResponse response = userService.updateUser(1L, updateUserRequest);

        // Assert
        assertNotNull(response);
        assertEquals(HttpStatus.BAD_REQUEST.value(), response.getStatusCode());
        assertEquals(HttpStatus.BAD_REQUEST.name(), response.getStatus());
        
        // Verify user was not updated
        assertNotEquals(updateUserRequest.getEmail(), user.getEmail());
    }

    @Test
    void updateUser_PartialUpdate() {
        // Arrange
        UpdateUserRequest partialRequest = new UpdateUserRequest();
        partialRequest.setUsername("partialupdate");
        // Other fields are null

        when(userRepository.findById(1L)).thenReturn(Optional.of(user));
        when(userRepository.existsByUsername(partialRequest.getUsername())).thenReturn(false);
        when(messageUtils.getMessage("success.user.updated", partialRequest.getUsername()))
                .thenReturn("User updated successfully: partialupdate");

        // Act
        MessageResponse response = userService.updateUser(1L, partialRequest);

        // Assert
        assertNotNull(response);
        assertEquals(HttpStatus.OK.value(), response.getStatusCode());
        assertEquals(HttpStatus.OK.name(), response.getStatus());
        
        // Verify only username was updated
        assertEquals(partialRequest.getUsername(), user.getUsername());
        assertEquals("test@example.com", user.getEmail()); // Unchanged
        assertEquals("USER", user.getRole()); // Unchanged
        verify(passwordEncoder, never()).encode(any());
    }

    @Test
    void deleteUser_Success() {
        // Arrange
        when(userRepository.existsById(1L)).thenReturn(true);
        when(messageUtils.getMessage("success.user.deleted", 1L))
                .thenReturn("User deleted successfully with ID: 1");

        // Act
        MessageResponse response = userService.deleteUser(1L);

        // Assert
        assertNotNull(response);
        assertEquals(HttpStatus.OK.value(), response.getStatusCode());
        assertEquals(HttpStatus.OK.name(), response.getStatus());
        
        // Verify user was deleted
        verify(userRepository).deleteById(1L);
    }

    @Test
    void deleteUser_UserNotFound() {
        // Arrange
        when(userRepository.existsById(999L)).thenReturn(false);
        when(messageUtils.getMessage("error.user.not.found", 999L))
                .thenReturn("User not found with ID: 999");

        // Act
        MessageResponse response = userService.deleteUser(999L);

        // Assert
        assertNotNull(response);
        assertEquals(HttpStatus.NOT_FOUND.value(), response.getStatusCode());
        assertEquals(HttpStatus.NOT_FOUND.name(), response.getStatus());
        
        // Verify delete was not called
        verify(userRepository, never()).deleteById(anyLong());
    }

    @SuppressWarnings("unchecked")
   @Test
    void getAllUsers_Success() {
        // Arrange
        Pageable pageable = PageRequest.of(0, 10);
        Page<User> userPage = new PageImpl<>(Arrays.asList(user));
        
        when(userRepository.findAll(any(Specification.class), eq(pageable))).thenReturn(userPage);
        when(modelMapper.map(user, UserDto.class)).thenReturn(userDto);
        when(messageUtils.getMessage("success.user.retrieved")).thenReturn("Users retrieved successfully");

        // Act
        ListResponse<UserDto> response = userService.getAllUsers(pageable, "test", "example", "USER");

        // Assert
        assertNotNull(response);
        assertEquals(HttpStatus.OK.value(), response.getStatusCode());
        assertEquals(HttpStatus.OK.name(), response.getStatus());
        assertEquals(1, response.getData().size());
        assertEquals(userDto, response.getData().get(0));
    }

    @Test
    void getUserById_Success() {
        // Arrange
        when(userRepository.findById(1L)).thenReturn(Optional.of(user));
        when(modelMapper.map(user, UserDto.class)).thenReturn(userDto);
        when(messageUtils.getMessage("success.user.retrieved")).thenReturn("User retrieved successfully");

        // Act
        ApiDataResponseBuilder response = userService.getUserById(1L);

        // Assert
        assertNotNull(response);
        assertEquals(HttpStatus.OK, response.getStatus());
        assertEquals(HttpStatus.OK.value(), response.getStatusCode());
        assertEquals(userDto, response.getData());
    }

    @Test
    void getUserById_UserNotFound() {
        // Arrange
        when(userRepository.findById(999L)).thenReturn(Optional.empty());
        when(messageUtils.getMessage("error.user.not.found", 999L))
                .thenReturn("User not found with ID: 999");

        // Act & Assert
        assertThrows(RuntimeException.class, () -> {
            userService.getUserById(999L);
        });
    }
}

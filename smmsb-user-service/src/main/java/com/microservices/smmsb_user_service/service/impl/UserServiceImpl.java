package com.microservices.smmsb_user_service.service.impl;

import java.util.List;

import org.modelmapper.ModelMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.http.HttpStatus;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
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
import com.microservices.smmsb_user_service.service.UserService;
import com.microservices.smmsb_user_service.utils.MessageUtils;
import com.microservices.smmsb_user_service.utils.UserSpecifications;

@Service
public class UserServiceImpl implements UserService {

      private final UserRepository userRepository;
      private final PasswordEncoder passwordEncoder;
      private final MessageUtils messageUtils;
      private final ModelMapper modelMapper;

      @Autowired
      public UserServiceImpl(UserRepository userRepository, PasswordEncoder passwordEncoder, MessageUtils messageUtils,
                  ModelMapper modelMapper) {
            this.userRepository = userRepository;
            this.passwordEncoder = passwordEncoder;
            this.messageUtils = messageUtils;
            this.modelMapper = modelMapper;
      }

      // Create User
      @Override
      @Transactional
      public MessageResponse createUser(CreateUserRequest createUserRequest) {
            // Check if username already exists
            if (userRepository.existsByUsername(createUserRequest.getUsername())) {
                  throw new DuplicateResourceException(
                              messageUtils.getMessage("error.user.username.already.exists",
                                          createUserRequest.getUsername()));
            }

            // Check if email already exists
            if (userRepository.existsByEmail(createUserRequest.getEmail())) {
                  throw new DuplicateResourceException(
                              messageUtils.getMessage("error.user.email.already.exists", createUserRequest.getEmail()));
            }

            // Create new user
            User user = modelMapper.map(createUserRequest, User.class);
            user.setPasswordHash(passwordEncoder.encode(createUserRequest.getPassword()));
            userRepository.save(user);

            return new MessageResponse(
                        messageUtils.getMessage("success.user.created", createUserRequest.getUsername()),
                        HttpStatus.CREATED.value(),
                        HttpStatus.CREATED.name());
      }

      // Update User
      @Override
      @Transactional
      public MessageResponse updateUser(Long Id, UpdateUserRequest updateUserRequest) {
            // Check if user exists
            User user = userRepository.findById(Id)
                        .orElseThrow(() -> new ResourceNotFoundException(
                                    messageUtils.getMessage("error.user.not.found")));

            // Update only if request data is provided
            if (updateUserRequest.getUsername() != null
                        && !updateUserRequest.getUsername().equals(user.getUsername())) {
                  if (userRepository.existsByUsername(updateUserRequest.getUsername())) {
                        throw new DuplicateResourceException(
                                    messageUtils.getMessage("error.user.username.already.exists",
                                                updateUserRequest.getUsername()));
                  }
                  user.setUsername(updateUserRequest.getUsername());
            }

            if (updateUserRequest.getEmail() != null
                        && !updateUserRequest.getEmail().equals(user.getEmail())) {
                  if (userRepository.existsByEmail(updateUserRequest.getEmail())) {
                        throw new DuplicateResourceException(
                                    messageUtils.getMessage("error.user.email.already.exists",
                                                updateUserRequest.getEmail()));
                  }
                  user.setEmail(updateUserRequest.getEmail());
            }

            if (updateUserRequest.getPassword() != null) {
                  user.setPasswordHash(passwordEncoder.encode(updateUserRequest.getPassword()));
            }

            if (updateUserRequest.getRole() != null) {
                  user.setRole(updateUserRequest.getRole());
            }

            // Simpan perubahan ke database
            userRepository.save(user);

            return new MessageResponse(
                        messageUtils.getMessage("success.user.updated", user.getUsername()),
                        HttpStatus.OK.value(),
                        HttpStatus.OK.name());
      }

      // Delete User
      @Override
      @Transactional
      public MessageResponse deleteUser(Long Id) {
            // Check if user exists
            if (!userRepository.existsById(Id)) {
                  throw new ResourceNotFoundException(
                              messageUtils.getMessage("error.user.not.found"));
            }
            // Delete user
            userRepository.deleteById(Id);

            return new MessageResponse(
                        messageUtils.getMessage("success.user.deleted", Id),
                        HttpStatus.OK.value(),
                        HttpStatus.OK.name());
      }

      // Get All Users
      @Override
      public ListResponse<UserDto> getAllUsers(Pageable pageable, String username, String email, String role) {
            Specification<User> spec = Specification.where(null);

            if (username != null) {
                  spec = spec.and(UserSpecifications.hasUsername(username));
            }
            if (email != null) {
                  spec = spec.and(UserSpecifications.hasEmail(email));
            }
            if (role != null) {
                  spec = spec.and(UserSpecifications.hasRole(role));
            }

            Page<User> users = userRepository.findAll(spec, pageable);
            List<UserDto> userDtos = users.stream()
                        .map(user -> modelMapper.map(user, UserDto.class))
                        .toList();

            return new ListResponse<>(
                        userDtos,
                        messageUtils.getMessage("success.user.retrieved"),
                        HttpStatus.OK.value(),
                        HttpStatus.OK.name());
      }

      // Get User By Id
      @Override
      public ApiDataResponseBuilder getUserById(Long id) {
            User user = userRepository.findById(id)
                        .orElseThrow(() -> new ResourceNotFoundException(
                                    messageUtils.getMessage("error.user.not.found")));

            UserDto userDto = modelMapper.map(user, UserDto.class);

            return ApiDataResponseBuilder.builder()
                        .data(userDto)
                        .message(messageUtils.getMessage("success.user.retrieved"))
                        .status(HttpStatus.OK)
                        .statusCode(HttpStatus.OK.value())
                        .build();
      }

}
package com.microservices.smmsb_user_service.service.impl;

import java.util.List;

import org.modelmapper.ModelMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.hateoas.EntityModel;
import org.springframework.hateoas.Link;
import org.springframework.hateoas.server.mvc.WebMvcLinkBuilder;
import org.springframework.http.HttpStatus;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.microservices.smmsb_user_service.controller.UserController;
import com.microservices.smmsb_user_service.dto.UserDto;
import com.microservices.smmsb_user_service.dto.request.CreateUserRequest;
import com.microservices.smmsb_user_service.dto.request.UpdateUserRequest;
import com.microservices.smmsb_user_service.dto.response.ApiDataResponseBuilder;
import com.microservices.smmsb_user_service.dto.response.ListResponse;
import com.microservices.smmsb_user_service.dto.response.MessageResponse;
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
                  return new MessageResponse(
                              messageUtils.getMessage("error.user.username.already.exists",
                                          createUserRequest.getUsername()),
                              HttpStatus.BAD_REQUEST.value(),
                              HttpStatus.BAD_REQUEST.name());
            }

            // Check if email already exists
            if (userRepository.existsByEmail(createUserRequest.getEmail())) {
                  return new MessageResponse(
                              messageUtils.getMessage("error.user.email.already.exists", createUserRequest.getEmail()),
                              HttpStatus.BAD_REQUEST.value(),
                              HttpStatus.BAD_REQUEST.name());
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
      public MessageResponse updateUser(int Id, UpdateUserRequest updateUserRequest) {
            // Check if user exists
            User user = userRepository.findById(Id)
                        .orElseThrow(() -> new RuntimeException(
                                    messageUtils.getMessage("error.user.not.found", Id)));

            // Check if username already exists
            if (!user.getUsername().equals(updateUserRequest.getUsername())
                        && userRepository.existsByUsername(updateUserRequest.getUsername())) {
                  return new MessageResponse(
                              messageUtils.getMessage("error.user.username.already.exists",
                                          updateUserRequest.getUsername()),
                              HttpStatus.BAD_REQUEST.value(),
                              HttpStatus.BAD_REQUEST.name());
            }
            // Check if email already exists
            if (!user.getEmail().equals(updateUserRequest.getEmail())
                        && userRepository.existsByEmail(updateUserRequest.getEmail())) {
                  return new MessageResponse(
                              messageUtils.getMessage("error.user.email.already.exists", updateUserRequest.getEmail()),
                              HttpStatus.BAD_REQUEST.value(),
                              HttpStatus.BAD_REQUEST.name());
            }

            // Update user jika ada request yang dikirim
            if (updateUserRequest.getUsername() != null) {
                  user.setUsername(updateUserRequest.getUsername());
            }
            if (updateUserRequest.getEmail() != null) {
                  user.setEmail(updateUserRequest.getEmail());
            }
            if (updateUserRequest.getPassword() != null) {
                  user.setPasswordHash(passwordEncoder.encode(updateUserRequest.getPassword()));
            }
            if (updateUserRequest.getRole() != null) {
                  user.setRole(updateUserRequest.getRole());
            }
            return new MessageResponse(
                        messageUtils.getMessage("success.user.updated", user.getUsername()),
                        HttpStatus.OK.value(),
                        HttpStatus.OK.name());
      }

      // Delete User
      @Override
      @Transactional
      public MessageResponse deleteUser(int Id) {
            // Check if user exists
            if (!userRepository.existsById(Id)) {
                  return new MessageResponse(
                              messageUtils.getMessage("error.user.not.found", Id),
                              HttpStatus.NOT_FOUND.value(),
                              HttpStatus.NOT_FOUND.name());
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
      public ListResponse<EntityModel<UserDto>> getAllUsers(Pageable pageable, String username, String email,
                  String role) {
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
            List<EntityModel<UserDto>> userResources = users.stream().map(user -> {
                  UserDto userDto = modelMapper.map(user, UserDto.class);

                  // Tambahkan HATEOAS links
                  Link selfLink = WebMvcLinkBuilder
                              .linkTo(WebMvcLinkBuilder.methodOn(UserController.class).getUserById(userDto.getId()))
                              .withSelfRel();
                  Link updateLink = WebMvcLinkBuilder
                              .linkTo(WebMvcLinkBuilder.methodOn(UserController.class).updateUser(userDto.getId(),
                                          null))
                              .withRel("update");
                  Link deleteLink = WebMvcLinkBuilder
                              .linkTo(WebMvcLinkBuilder.methodOn(UserController.class).deleteUser(userDto.getId()))
                              .withRel("delete");

                  return EntityModel.of(userDto, selfLink, updateLink, deleteLink);
            }).toList();

            return new ListResponse<>(
                        userResources,
                        messageUtils.getMessage("success.user.retrieved"),
                        HttpStatus.OK.value(),
                        HttpStatus.OK.name());
      }

      // Get User By Id
      @Override
      public ApiDataResponseBuilder getUserById(int id) {
            User user = userRepository.findById(id)
                        .orElseThrow(() -> new RuntimeException(
                                    messageUtils.getMessage("error.user.not.found", id)));

            UserDto userDto = modelMapper.map(user, UserDto.class);

            // Tambahkan HATEOAS links
            Link selfLink = WebMvcLinkBuilder.linkTo(WebMvcLinkBuilder.methodOn(UserController.class).getUserById(id))
                        .withSelfRel();
            Link allUsersLink = WebMvcLinkBuilder
                        .linkTo(WebMvcLinkBuilder.methodOn(UserController.class).getAllUsers(null, null, null, null))
                        .withRel("allUsers");
            Link updateLink = WebMvcLinkBuilder
                        .linkTo(WebMvcLinkBuilder.methodOn(UserController.class).updateUser(id, null))
                        .withRel("update");
            Link deleteLink = WebMvcLinkBuilder.linkTo(WebMvcLinkBuilder.methodOn(UserController.class).deleteUser(id))
                        .withRel("delete");

            EntityModel<UserDto> userResource = EntityModel.of(userDto, selfLink, allUsersLink, updateLink, deleteLink);

            return ApiDataResponseBuilder.builder()
                        .data(userResource)
                        .message(messageUtils.getMessage("success.user.retrieved"))
                        .status(HttpStatus.OK)
                        .statusCode(HttpStatus.OK.value())
                        .build();
      }
}
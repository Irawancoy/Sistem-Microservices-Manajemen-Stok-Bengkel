package com.microservices.smmsb_user_service.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.hateoas.EntityModel;
import org.springframework.hateoas.Link;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.microservices.smmsb_user_service.dto.UserDto;
import com.microservices.smmsb_user_service.dto.request.CreateUserRequest;
import com.microservices.smmsb_user_service.dto.request.UpdateUserRequest;
import com.microservices.smmsb_user_service.dto.response.ListResponse;
import com.microservices.smmsb_user_service.dto.response.MessageResponse;
import com.microservices.smmsb_user_service.service.UserService;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;

import static org.springframework.hateoas.server.mvc.WebMvcLinkBuilder.*;

import java.util.List;

@RestController
@RequestMapping("/api/v1/users")
@PreAuthorize("hasRole('SUPERADMIN')")
@Tag(name = "Users", description = "API operations related to user management")
public class UserController {

      private final UserService userService;

      @Autowired
      public UserController(UserService userService) {
            this.userService = userService;
      }

      // Create User
      @PostMapping("/create")
      @Operation(summary = "Create a new user", description = "Creates a new user account.", tags = { "Users" })
      @ApiResponses(value = {
                  @ApiResponse(responseCode = "201", description = "User created successfully", content = @Content(schema = @Schema(implementation = MessageResponse.class))),
                  @ApiResponse(responseCode = "400", description = "Bad request (validation error)", content = @Content(schema = @Schema(implementation = MessageResponse.class))),
                  @ApiResponse(responseCode = "409", description = "Username or email already exists", content = @Content(schema = @Schema(implementation = MessageResponse.class))),
                  @ApiResponse(responseCode = "403", description = "Forbidden", content = @Content(schema = @Schema(implementation = MessageResponse.class)))
      })
      public MessageResponse createUser(CreateUserRequest createUserRequest) {
            return userService.createUser(createUserRequest);
      }

      // Update User
      @PutMapping("/update/{id}")
      @Operation(summary = "Update a user", description = "Updates an existing user's information.", tags = {
                  "Users" })
      @ApiResponses(value = {
                  @ApiResponse(responseCode = "200", description = "User updated successfully", content = @Content(schema = @Schema(implementation = MessageResponse.class))),
                  @ApiResponse(responseCode = "400", description = "Bad request (validation error)", content = @Content(schema = @Schema(implementation = MessageResponse.class))),
                  @ApiResponse(responseCode = "404", description = "User not found", content = @Content),
                  @ApiResponse(responseCode = "409", description = "Email already exists", content = @Content(schema = @Schema(implementation = MessageResponse.class))),
                  @ApiResponse(responseCode = "403", description = "Forbidden", content = @Content(schema = @Schema(implementation = MessageResponse.class)))
      })
      public MessageResponse updateUser(@PathVariable int id, UpdateUserRequest updateUserRequest) {
            return userService.updateUser(id, updateUserRequest);
      }

      // Delete User
      @DeleteMapping("/delete/{id}")
      @Operation(summary = "Delete a user", description = "Deletes a user by their ID.", tags = { "Users" })
      @ApiResponses(value = {
                  @ApiResponse(responseCode = "200", description = "User deleted successfully", content = @Content(schema = @Schema(implementation = MessageResponse.class))),
                  @ApiResponse(responseCode = "404", description = "User not found", content = @Content),
                  @ApiResponse(responseCode = "403", description = "Forbidden", content = @Content(schema = @Schema(implementation = MessageResponse.class))) })
      public MessageResponse deleteUser(@PathVariable int id) {
            return userService.deleteUser(id);
      }

      // Get All Users
      @GetMapping("/get-all")
      @Operation(summary = "Get all users", description = "Retrieves a paginated list of all users. Supports filtering.", tags = {
                  "Users" })
      @ApiResponses(value = {
                  @ApiResponse(responseCode = "200", description = "Successfully retrieved users", content = @Content(schema = @Schema(implementation = ListResponse.class))),
                  @ApiResponse(responseCode = "403", description = "Forbidden", content = @Content(schema = @Schema(implementation = MessageResponse.class)))
      })
      public ResponseEntity<ListResponse<EntityModel<UserDto>>> getAllUsers(
                  @PageableDefault(size = 10) Pageable pageable,
                  @RequestParam(required = false) String username,
                  @RequestParam(required = false) String email,
                  @RequestParam(required = false) String role) {
            ListResponse<UserDto> usersResponse = userService.getAllUsers(pageable, username, email, role);
            List<EntityModel<UserDto>> userResources = usersResponse.getData().stream().map(
                        user -> {
                              Link selfLink = linkTo(methodOn(UserController.class).getUserById(user.getId()))
                                          .withSelfRel();
                              Link updateLink = linkTo(methodOn(UserController.class).updateUser(user.getId(), null))
                                          .withRel("update");
                              Link deleteLink = linkTo(methodOn(UserController.class).deleteUser(user.getId()))
                                          .withRel("delete");
                              return EntityModel.of(user, selfLink, updateLink, deleteLink);
                        }).toList();

            ListResponse<EntityModel<UserDto>> listResponse = new ListResponse<>(
                        userResources, usersResponse.getMessage(), usersResponse.getStatusCode(),
                        usersResponse.getStatus());
            return ResponseEntity.ok(listResponse);
      }

      // Get User By Id
      @GetMapping("/get-by-id/{id}")
      @Operation(summary = "Get user by ID", description = "Retrieves a user by their ID.", tags = { "Users" })
      @ApiResponses(value = {
                  @ApiResponse(responseCode = "200", description = "Successfully retrieved user", content = @Content(schema = @Schema(implementation = UserDto.class))),
                  @ApiResponse(responseCode = "404", description = "User not found", content = @Content),
                  @ApiResponse(responseCode = "403", description = "Forbidden", content = @Content(schema = @Schema(implementation = MessageResponse.class)))
      })
      public ResponseEntity<EntityModel<UserDto>> getUserById(@PathVariable int id) {
            UserDto user = userService.getUserById(id);
            if (user == null) {
                  return ResponseEntity.notFound().build();
            }
            Link selfLink = linkTo(methodOn(UserController.class).getUserById(id)).withSelfRel();
            Link allUserLink = linkTo(methodOn(UserController.class).getAllUsers(null, null, null, null)).withRel("allUsers");
            Link updateLink = linkTo(methodOn(UserController.class).updateUser(id, null)).withRel("update");
            Link deleteLink = linkTo(methodOn(UserController.class).deleteUser(id)).withRel("delete");
            EntityModel<UserDto> userResource = EntityModel.of(user,allUserLink, selfLink, updateLink, deleteLink);
            return ResponseEntity.ok(userResource);
      }
}

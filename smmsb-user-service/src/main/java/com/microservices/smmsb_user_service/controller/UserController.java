package com.microservices.smmsb_user_service.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.data.domain.Sort;

import com.microservices.smmsb_user_service.dto.UserDto;
import com.microservices.smmsb_user_service.dto.request.CreateUserRequest;
import com.microservices.smmsb_user_service.dto.request.UpdateUserRequest;
import com.microservices.smmsb_user_service.dto.response.ApiDataResponseBuilder;
import com.microservices.smmsb_user_service.dto.response.MessageResponse;
import com.microservices.smmsb_user_service.dto.response.ListResponse;
import com.microservices.smmsb_user_service.exception.ValidationErrorResponse;
import com.microservices.smmsb_user_service.service.UserService;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.parameters.RequestBody;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;

@RestController
@RequestMapping("/api/v1/users")
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
                  @ApiResponse(responseCode = "400", description = "Bad request (validation error)", content = @Content(schema = @Schema(implementation = ValidationErrorResponse.class))),
                  @ApiResponse(responseCode = "409", description = "Username or email already exists", content = @Content(schema = @Schema(implementation = MessageResponse.class))),
                  @ApiResponse(responseCode = "403", description = "Forbidden", content = @Content(schema = @Schema(implementation = MessageResponse.class))),
      })
      public MessageResponse createUser(@Valid @RequestBody CreateUserRequest createUserRequest) {
            return userService.createUser(createUserRequest);
      }

      // Update User
      @PutMapping("/update/{id}")
      @Operation(summary = "Update a user", description = "Updates an existing user's information.", tags = { "Users" })
      @ApiResponses(value = {
                  @ApiResponse(responseCode = "200", description = "User updated successfully", content = @Content(schema = @Schema(implementation = MessageResponse.class))),
                  @ApiResponse(responseCode = "400", description = "Bad request (validation error)", content = @Content(schema = @Schema(implementation = ValidationErrorResponse.class))),
                  @ApiResponse(responseCode = "404", description = "User not found", content = @Content(schema = @Schema(implementation = MessageResponse.class))),
                  @ApiResponse(responseCode = "409", description = "Username or Email already exists", content = @Content(schema = @Schema(implementation = MessageResponse.class))),
                  @ApiResponse(responseCode = "403", description = "Forbidden", content = @Content(schema = @Schema(implementation = MessageResponse.class)))
      })
      public MessageResponse updateUser(
                  @PathVariable Long id,
                  @Valid @RequestBody UpdateUserRequest updateUserRequest) {
            return userService.updateUser(id, updateUserRequest);
      }

      // Delete User
      @DeleteMapping("/delete/{id}")
      @Operation(summary = "Delete a user", description = "Deletes a user by their ID.", tags = { "Users" })
      @ApiResponses(value = {
                  @ApiResponse(responseCode = "200", description = "User deleted successfully", content = @Content(schema = @Schema(implementation = MessageResponse.class))),
                  @ApiResponse(responseCode = "404", description = "User not found", content = @Content(schema = @Schema(implementation = MessageResponse.class))),
                  @ApiResponse(responseCode = "403", description = "Forbidden", content = @Content(schema = @Schema(implementation = MessageResponse.class))) })
      public MessageResponse deleteUser(@PathVariable Long id) {
            return userService.deleteUser(id);
      }

      // Get All Users
      @GetMapping("/get-all")
      @Operation(summary = "Get all users", description = "Retrieves a paginated list of all users with optional filters.", tags = {
                  "Users" })
      @ApiResponses(value = {
                  @ApiResponse(responseCode = "200", description = "Successfully retrieved users", content = @Content(mediaType = "application/json", schema = @Schema(implementation = ListResponse.class))),
                  @ApiResponse(responseCode = "403", description = "Forbidden", content = @Content(schema = @Schema(implementation = MessageResponse.class)))
      })
      public ResponseEntity<ListResponse<UserDto>> getAllUsers(
                  @Parameter(
                    description = "Pagination and sorting parameters",
                    example = "{ \"page\": 0, \"size\": 10, \"sort\": \"id,asc\" }"
            )
                  @PageableDefault(page = 0, size = 10, sort = "id", direction = Sort.Direction.ASC) Pageable pageable,

                  @Parameter(description = "Filter users by username") @RequestParam(required = false) String username,

                  @Parameter(description = "Filter users by email") @RequestParam(required = false) String email,

                  @Parameter(description = "Filter users by role") @RequestParam(required = false) String role) {

            return ResponseEntity.ok(userService.getAllUsers(pageable, username, email, role));
      }

      // Get User By Id
      @GetMapping("/get-by-id/{id}")
      @Operation(summary = "Get user by ID", description = "Retrieves a user by their ID.", tags = { "Users" })
      @ApiResponses(value = {
                  @ApiResponse(responseCode = "200", description = "Successfully retrieved user", content = @Content(schema = @Schema(implementation = UserDto.class))),
                  @ApiResponse(responseCode = "404", description = "User not found", content = @Content(schema = @Schema(implementation = MessageResponse.class))),
                  @ApiResponse(responseCode = "403", description = "Forbidden", content = @Content(schema = @Schema(implementation = MessageResponse.class)))
      })
      public ResponseEntity<ApiDataResponseBuilder> getUserById(@PathVariable Long id) {
            return ResponseEntity.ok(userService.getUserById(id));
      }
}

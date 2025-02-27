package com.microservices.smmsb_user_service.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.microservices.smmsb_user_service.dto.request.LoginRequest;
import com.microservices.smmsb_user_service.dto.response.LoginResponse;
import com.microservices.smmsb_user_service.service.AuthService;
import com.microservices.smmsb_user_service.dto.response.MessageResponse;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponses;


@RestController
@RequestMapping("/api/v1/auth")
@Tag(name = "Authentication", description = "Authentication API for login")
public class AuthController {
   private final AuthService authService;

   @Autowired
   public AuthController(AuthService authService) {
      this.authService = authService;
   }
   
   //Login
   @PostMapping("/login")
   @Operation(summary = "Login", description = "Authentication API for login and return JWT token.", tags = {
         "Authentication" })
    @ApiResponses(value = {
            @ApiResponse(responseCode = "201", description = "User registered successfully", content = @Content(schema = @Schema(implementation = MessageResponse.class))),
            @ApiResponse(responseCode = "400", description = "Bad request (validation error)", content = @Content(schema = @Schema(implementation = MessageResponse.class))),
            @ApiResponse(responseCode = "409", description = "Username or email already exists", content = @Content(schema = @Schema(implementation = MessageResponse.class)))
    })
   public ResponseEntity<LoginResponse> login(@RequestBody LoginRequest loginRequest) {
      LoginResponse loginResponse = authService.login(loginRequest);
      return ResponseEntity.ok(loginResponse);
   }
   
}

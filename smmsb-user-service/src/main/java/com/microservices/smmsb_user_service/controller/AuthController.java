package com.microservices.smmsb_user_service.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.microservices.smmsb_user_service.dto.request.LoginRequest;
import com.microservices.smmsb_user_service.service.AuthService;
import com.microservices.smmsb_user_service.exception.ValidationErrorResponse;
import com.microservices.smmsb_user_service.dto.response.ApiDataResponseBuilder;
import com.microservices.smmsb_user_service.dto.response.MessageResponse;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
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

   // Login
   @PostMapping("/login")
   @Operation(summary = "Login", description = "Authenticate user and return JWT token.", tags = { "Authentication" })
   @ApiResponses(value = {
         @ApiResponse(responseCode = "200", description = "Login successful", content = @Content(schema = @Schema(implementation = ApiDataResponseBuilder.class))),
         @ApiResponse(responseCode = "400", description = "Bad request (validation error)", content = @Content(schema = @Schema(implementation = ValidationErrorResponse.class))),
         @ApiResponse(responseCode = "401", description = "Unauthorized (Invalid credentials)", content = @Content(schema = @Schema(implementation = MessageResponse.class)))
   })
   public ResponseEntity<Object> login(@Valid @RequestBody LoginRequest loginRequest) {
      ApiDataResponseBuilder result = authService.login(loginRequest);
      return ResponseEntity.status(result.getStatus()).body(result);
   }

}

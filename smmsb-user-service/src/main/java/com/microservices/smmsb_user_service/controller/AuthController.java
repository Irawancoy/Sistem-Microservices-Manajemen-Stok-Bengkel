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

@RestController
@RequestMapping("/api/v1/auth")
public class AuthController {
   private final AuthService authService;

   @Autowired
   public AuthController(AuthService authService) {
      this.authService = authService;
   }
   
   //Login
   @PostMapping("/login")
   public ResponseEntity<LoginResponse> login(@RequestBody LoginRequest loginRequest) {
      LoginResponse loginResponse = authService.login(loginRequest);
      return ResponseEntity.ok(loginResponse);
   }
   
}

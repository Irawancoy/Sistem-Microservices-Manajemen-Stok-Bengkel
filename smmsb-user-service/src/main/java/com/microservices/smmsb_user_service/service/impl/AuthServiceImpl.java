package com.microservices.smmsb_user_service.service.impl;

import org.springframework.security.core.Authentication;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;

import com.microservices.smmsb_user_service.dto.request.LoginRequest;
import com.microservices.smmsb_user_service.dto.response.ApiDataResponseBuilder;
import com.microservices.smmsb_user_service.dto.response.LoginResponse;
import com.microservices.smmsb_user_service.model.AuthSession;
import com.microservices.smmsb_user_service.model.User;
import com.microservices.smmsb_user_service.repository.AuthSessionRepository;
import com.microservices.smmsb_user_service.repository.UserRepository;
import com.microservices.smmsb_user_service.service.AuthService;
import com.microservices.smmsb_user_service.security.JwtUtil;
import com.microservices.smmsb_user_service.utils.MessageUtils;

@Service
public class AuthServiceImpl implements AuthService {

   private final AuthenticationManager authenticationManager;
   private final JwtUtil jwtUtil;
   private final UserRepository userRepository;
   private final MessageUtils messageUtils;
   private final AuthSessionRepository authSessionRepository;

   @Autowired
   public AuthServiceImpl(AuthenticationManager authenticationManager, JwtUtil jwtUtil, UserRepository userRepository, MessageUtils messageUtils, AuthSessionRepository authSessionRepository) {
      this.authenticationManager = authenticationManager;
      this.jwtUtil = jwtUtil;
      this.userRepository = userRepository;
      this.messageUtils = messageUtils;
      this.authSessionRepository = authSessionRepository;
   }

  @Override
public ApiDataResponseBuilder login(LoginRequest loginRequest) {
    // Lakukan autentikasi pengguna
    Authentication authentication = authenticationManager.authenticate(
        new UsernamePasswordAuthenticationToken(loginRequest.getUsername(), loginRequest.getPassword())
    );

    if (authentication.isAuthenticated()) {
        // Cari pengguna berdasarkan username
        User user = userRepository.findByUsername(loginRequest.getUsername())
            .orElseThrow(() -> new UsernameNotFoundException(messageUtils.getMessage("error.login.invalid.request")));

        // Generate token JWT
        String token = jwtUtil.generateToken(authentication);

        // Cek jika sudah ada sesi sebelumnya dengan username yang sama dan hapus
        if (authSessionRepository.existsById(loginRequest.getUsername())) {
            authSessionRepository.deleteById(loginRequest.getUsername());
        }

        // Simpan sesi login baru dengan username sebagai key
        AuthSession authSession = new AuthSession(loginRequest.getUsername(), token, user.getRole());
        authSessionRepository.save(authSession);

        // Bungkus LoginResponse dalam ApiDataResponseBuilder
        LoginResponse loginResponse = new LoginResponse(token, user.getUsername(), user.getRole());
        return ApiDataResponseBuilder.builder()
            .data(loginResponse)
            .message(messageUtils.getMessage("success.login"))
            .statusCode(200)
            .status(HttpStatus.OK)
            .build();
    } else {
        throw new UsernameNotFoundException(messageUtils.getMessage("error.login.invalid.request"));
    }
}

   
   
}

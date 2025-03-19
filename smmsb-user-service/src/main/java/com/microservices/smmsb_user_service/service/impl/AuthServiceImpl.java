package com.microservices.smmsb_user_service.service.impl;

import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Service;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;

import com.microservices.smmsb_user_service.dto.request.LoginRequest;
import com.microservices.smmsb_user_service.dto.response.ApiDataResponseBuilder;
import com.microservices.smmsb_user_service.dto.response.LoginResponse;
import com.microservices.smmsb_user_service.exception.ResourceNotFoundException;
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
    public AuthServiceImpl(AuthenticationManager authenticationManager, JwtUtil jwtUtil, UserRepository userRepository,
            MessageUtils messageUtils, AuthSessionRepository authSessionRepository) {
        this.authenticationManager = authenticationManager;
        this.jwtUtil = jwtUtil;
        this.userRepository = userRepository;
        this.messageUtils = messageUtils;
        this.authSessionRepository = authSessionRepository;
    }

    @Override
    public ApiDataResponseBuilder login(LoginRequest loginRequest) {
        try {
            // Lakukan autentikasi pengguna
            Authentication authentication = authenticationManager.authenticate(
                    new UsernamePasswordAuthenticationToken(loginRequest.getUsername(), loginRequest.getPassword()));

            // Cari pengguna berdasarkan username
            User user = userRepository.findByUsername(loginRequest.getUsername())
                    .orElseThrow(() -> new ResourceNotFoundException(
                            messageUtils.getMessage("error.login.username.not.found")));

            // Generate token JWT
            String token = jwtUtil.generateToken(authentication, user.getId());

            // Simpan sesi login baru dengan username sebagai key
            AuthSession authSession = new AuthSession(user.getUsername(), token, user.getRole());
            authSessionRepository.save(authSession);

            // Bungkus LoginResponse dalam ApiDataResponseBuilder
            LoginResponse loginResponse = new LoginResponse(token, user.getUsername(), user.getRole(),
                    authSession.getSessionId());
            return ApiDataResponseBuilder.builder()
                    .data(loginResponse)
                    .message(messageUtils.getMessage("success.login"))
                    .statusCode(HttpStatus.OK.value())
                    .status(HttpStatus.OK)
                    .build();
        } catch (BadCredentialsException ex) {
            throw new BadCredentialsException(messageUtils.getMessage("error.login.invalid.credentials"));
        }
    }

}

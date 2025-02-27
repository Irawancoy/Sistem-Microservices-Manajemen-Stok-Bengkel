package com.microservices.smmsb_user_service.service;

import com.microservices.smmsb_user_service.dto.request.LoginRequest;
import com.microservices.smmsb_user_service.dto.response.ApiDataResponseBuilder;

public interface AuthService {
   ApiDataResponseBuilder login(LoginRequest loginRequest);
}

package com.microservices.smmsb_user_service.service;

import org.springframework.data.domain.Pageable;
import org.springframework.hateoas.EntityModel;

import com.microservices.smmsb_user_service.dto.UserDto;
import com.microservices.smmsb_user_service.dto.request.CreateUserRequest;
import com.microservices.smmsb_user_service.dto.request.UpdateUserRequest;
import com.microservices.smmsb_user_service.dto.response.ApiDataResponseBuilder;
import com.microservices.smmsb_user_service.dto.response.ListResponse;
import com.microservices.smmsb_user_service.dto.response.MessageResponse;

import jakarta.servlet.http.HttpServletRequest;

public interface UserService {
   MessageResponse createUser(CreateUserRequest createUserRequest,HttpServletRequest request);

   MessageResponse updateUser(int id, UpdateUserRequest updateUserRequest);

   MessageResponse deleteUser(int id);

   ListResponse<EntityModel<UserDto>> getAllUsers(Pageable pageable, String username, String email, String role);

   ApiDataResponseBuilder getUserById(int id);
   
}

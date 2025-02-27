package com.microservices.smmsb_user_service.dto.request;

import lombok.Data;

@Data
public class UpdateUserRequest {
   private String username;
   private String password;
   private String email;
   private String role;
}

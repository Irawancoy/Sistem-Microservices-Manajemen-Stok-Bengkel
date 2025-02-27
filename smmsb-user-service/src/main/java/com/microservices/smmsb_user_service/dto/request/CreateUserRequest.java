package com.microservices.smmsb_user_service.dto.request;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;

@Data
public class CreateUserRequest {
   @NotBlank(message = "Username is required")
   private String username;
   @NotBlank(message = "Password is required")
   private String password;
   @NotBlank(message = "Email is required")
   private String email;
   @NotBlank(message = "Role is required")
   private String role;
}

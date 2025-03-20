package com.microservices.smmsb_user_service.dto.request;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.Size;
import lombok.Data;

@Data
public class UpdateUserRequest {
   private String username;

   @Size(min = 8, message = "Password must be at least 8 characters long")
   private String password;

   @Email(message = "Invalid email format")
   private String email;

   private String role;
}

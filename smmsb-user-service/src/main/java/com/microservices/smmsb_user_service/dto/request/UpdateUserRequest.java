package com.microservices.smmsb_user_service.dto.request;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Data;

@Data
public class UpdateUserRequest {
   @NotBlank(message = "Username is required")
   private String username;

   @Size(min = 8, message = "Password must be at least 8 characters long")
   @NotBlank(message = "Password is required")
   private String password;

   @Email(message = "Invalid email format")
   @NotBlank(message = "Email is required")
   private String email;

   @NotBlank(message = "Role is required")
   private String role;
}

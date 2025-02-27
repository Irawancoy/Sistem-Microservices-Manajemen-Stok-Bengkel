package com.microservices.smmsb_user_service.dto;

import lombok.Data;

@Data
public class UserDto {
   private int id;
   private String username;
   private String passwordHash;
   private String email;
   private String role;
   
}

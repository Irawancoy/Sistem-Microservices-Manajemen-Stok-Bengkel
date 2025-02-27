package com.microservices.smmsb_user_service.model;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import org.springframework.data.annotation.Id;
import org.springframework.data.redis.core.RedisHash;

import java.io.Serializable;

@Data
@AllArgsConstructor
@NoArgsConstructor
@RedisHash("AuthSession")  // Redis hash dengan nama AuthSession
public class AuthSession implements Serializable {
   @Id
   private String username;  // Username sebagai key
   private String token;
   private String role;
}

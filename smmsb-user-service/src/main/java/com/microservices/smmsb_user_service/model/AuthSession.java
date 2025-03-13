package com.microservices.smmsb_user_service.model;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import org.springframework.data.annotation.Id;
import org.springframework.data.redis.core.RedisHash;
import org.springframework.data.redis.core.TimeToLive;

import java.io.Serializable;
import java.util.UUID;
import java.util.concurrent.TimeUnit;

@Data
@AllArgsConstructor
@NoArgsConstructor
@RedisHash("AuthSession")  // Redis hash dengan nama AuthSession
public class AuthSession implements Serializable {
   @Id
   private String sessionId; // Unique Key
   private String username;  
   private String token;
   private String role;

   @TimeToLive
   private Long expiration = TimeUnit.MINUTES.toSeconds(60);

   public AuthSession(String username, String token, String role) {
      this.sessionId = UUID.randomUUID().toString();
      this.username = username;
      this.token = token;
      this.role = role;
   }
}

package com.microservices.smmsb_inventory_service.config;

import io.minio.MinioClient;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class MinioConfig {

   @Value("${application.minio.url}") // Tetap gunakan prefix 'application'
   private String url;

   @Value("${application.minio.username}") // Tetap gunakan prefix 'application'
   private String username;

   @Value("${application.minio.password}") // Tetap gunakan prefix 'application'
   private String password;

   @Bean
   public MinioClient minioClient() {
      return MinioClient.builder()
            .endpoint(url)
            .credentials(username, password)
            .build();
   }

}
package com.microservices.smmsb_notofication_service.dto;

import lombok.Data;

@Data
public class NotificationDto {
   private Long id;
   private Long userId;
    private String message;
    private String type;
}

package com.microservices.smmsb_inventory_service.dto.kafkaEvent;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class NotificationEvent {
    private Long userId;     
    private String message;    
    private String type;         // Jenis notifikasi (EMAIL, PUSH, SMS, dll.)
}
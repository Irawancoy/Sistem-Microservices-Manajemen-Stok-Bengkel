package com.microservices.smmsb_inventory_service.dto.kafkaEvent;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class LowStockAlertEvent {
   private Long productId;
   private int quantity;
   private String productName;
   private String message;
}

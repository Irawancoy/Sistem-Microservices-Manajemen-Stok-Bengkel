package com.microservices.smmsb_inventory_service.dto.kafkaEvent;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class UpdateProductStockEvent {
    private Long userId;
    private Long productId;
    private int quantity;
}
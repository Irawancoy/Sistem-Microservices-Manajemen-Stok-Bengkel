package com.microservices.smmsb_inventory_service.dto.request;

import java.math.BigDecimal;

import lombok.Data;

@Data
public class UpdateProductStockRequest {
   private String productName;

   private String description;

   private int quantity;

   private BigDecimal price;

}

package com.microservices.smmsb_inventory_service.dto;

import java.math.BigDecimal;

import lombok.Data;

@Data
public class ProductStockDto {
   private int id;
   private String productName;
   private String description;
   private int quantity;
   private BigDecimal price;
   private String imageUrl;
}

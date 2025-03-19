package com.microservices.smmsb_inventory_service.dto.request;

import lombok.Data;

import java.math.BigDecimal;

import jakarta.validation.constraints.NotBlank;

@Data
public class CreateProductStockRequest {

   @NotBlank(message = "Product Name is required")
   private String productName;

   private String description;

   @NotBlank(message = "Quantity is required")
   private int quantity;
   
   @NotBlank(message = "Price is required")
   private BigDecimal price;

}

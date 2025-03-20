package com.microservices.smmsb_inventory_service.dto.request;

import lombok.Data;

import java.math.BigDecimal;

import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

@Data
public class CreateProductStockRequest {
   @NotBlank(message = "Product name is required")
   private String productName;

   @NotBlank(message = "Description is required")
   private String description;

   @Min(value = 1, message = "Quantity must be greater than 0")
   private int quantity;

   @NotNull(message = "Price is required")
   @DecimalMin(value = "0.0", inclusive = false, message = "Price must be greater than 0")
   private BigDecimal price;
}

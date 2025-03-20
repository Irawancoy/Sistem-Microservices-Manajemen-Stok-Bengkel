package com.microservices.smmsb_inventory_service.dto.request;

import java.math.BigDecimal;

import lombok.Data;
import jakarta.validation.constraints.DecimalMin;

@Data
public class UpdateProductStockRequest {

   private String productName;

   private String description;

   private int quantity;
   
   @DecimalMin(value = "0.0", inclusive = false, message = "Price must be greater than 0")
   private BigDecimal price;

}

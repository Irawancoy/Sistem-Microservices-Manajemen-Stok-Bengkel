package com.microservices.smmsb_transaction_service.dto.request;

import lombok.Data;
import jakarta.validation.constraints.NotBlank;

@Data
public class CreateTransactionRequest {
    @NotBlank(message = "Product ID is required")
    private Long productId;
    @NotBlank(message = "Quantity is required")
    private Integer quantity;
    
   
}

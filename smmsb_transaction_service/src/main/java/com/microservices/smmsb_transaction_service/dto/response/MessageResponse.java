package com.microservices.smmsb_transaction_service.dto.response;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class MessageResponse {
   private String message;
   private int statusCode;
   private String status;
}

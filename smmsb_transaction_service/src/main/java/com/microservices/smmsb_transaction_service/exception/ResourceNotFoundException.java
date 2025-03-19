package com.microservices.smmsb_transaction_service.exception;

public class ResourceNotFoundException extends RuntimeException {
   public ResourceNotFoundException(String message) {
       super(message);
   }
}

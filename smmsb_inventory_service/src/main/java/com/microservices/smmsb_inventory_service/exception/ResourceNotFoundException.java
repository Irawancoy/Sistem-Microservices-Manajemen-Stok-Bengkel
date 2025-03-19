package com.microservices.smmsb_inventory_service.exception;
public class ResourceNotFoundException extends RuntimeException {
   public ResourceNotFoundException(String message) {
       super(message);
   }
}

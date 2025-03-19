package com.microservices.smmsb_transaction_service.exception;

public class BadRequestException extends RuntimeException{
   public BadRequestException(String message) {
      super(message);
  }
}

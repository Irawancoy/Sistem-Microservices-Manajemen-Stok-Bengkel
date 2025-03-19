package com.microservices.smmsb_notofication_service.exception;
public class BadRequestException extends RuntimeException{
   public BadRequestException(String message) {
      super(message);
  }
}

package com.microservices.smmsb_notofication_service.service;

public interface EmailService {
   void sendEmail(String to, String subject, String body);
}
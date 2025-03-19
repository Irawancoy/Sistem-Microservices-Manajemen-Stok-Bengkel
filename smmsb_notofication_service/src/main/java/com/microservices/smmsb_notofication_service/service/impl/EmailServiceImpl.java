package com.microservices.smmsb_notofication_service.service.impl;

import jakarta.mail.MessagingException;
import jakarta.mail.internet.MimeMessage;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.stereotype.Service;

@Service
public class EmailServiceImpl {

    private final JavaMailSender mailSender;
    private final String defaultFromAddress;

    @Autowired
    public EmailServiceImpl(JavaMailSender mailSender) {
        this.mailSender = mailSender;
        this.defaultFromAddress = "noreply@example.com";
    }

    public void sendEmail(String to, String subject, String text) throws MessagingException {
        MimeMessage message = createMimeMessage(to, subject, text, defaultFromAddress);
        mailSender.send(message);
    }

    // Extracted method for better testability
    protected MimeMessage createMimeMessage(String to, String subject, String text, String from) throws MessagingException {
        MimeMessage message = mailSender.createMimeMessage();
        MimeMessageHelper helper = new MimeMessageHelper(message, true);

        helper.setTo(to);
        helper.setSubject(subject);
        helper.setText(text, true);
        helper.setFrom(from);

        return message;
    }
}

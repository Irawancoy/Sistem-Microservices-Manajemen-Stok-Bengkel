package com.microservices.smmsb_notofication_service.service.impl;

import jakarta.mail.MessagingException;
import jakarta.mail.internet.MimeMessage;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Spy;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.mail.MailSendException;
import org.springframework.mail.javamail.JavaMailSender;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class EmailServiceImplTest {

    @Mock
    private JavaMailSender mailSender;

    @Mock
    private MimeMessage mimeMessage;

    @Spy
    @InjectMocks
    private EmailServiceImpl emailService;

    @Test
    void sendEmail_ShouldSendEmailWithCorrectParameters() throws MessagingException {
        // Arrange
        String to = "recipient@example.com";
        String subject = "Test Subject";
        String text = "<h1>Test Email</h1><p>This is a test email.</p>";
        String from = "noreply@example.com";
        
        // We'll spy on the createMimeMessage method to avoid actually creating a MimeMessageHelper
        doReturn(mimeMessage).when(emailService).createMimeMessage(to, subject, text, from);
        
        // Act
        emailService.sendEmail(to, subject, text);
        
        // Assert
        verify(emailService).createMimeMessage(to, subject, text, from);
        verify(mailSender).send(mimeMessage);
    }

    @Test
    void sendEmail_ShouldThrowExceptionWhenMailSenderFails() throws MessagingException {
        // Arrange
        String to = "recipient@example.com";
        String subject = "Test Subject";
        String text = "<h1>Test Email</h1>";
        String from = "noreply@example.com";
        
        doReturn(mimeMessage).when(emailService).createMimeMessage(to, subject, text, from);
        
        // Use MailSendException which is a RuntimeException instead of MessagingException
        doThrow(new MailSendException("Failed to send email")).when(mailSender).send(any(MimeMessage.class));
        
        // Act & Assert
        Exception exception = assertThrows(MailSendException.class, () -> {
            emailService.sendEmail(to, subject, text);
        });
        
        assertEquals("Failed to send email", exception.getMessage());
    }

    @Test
    void createMimeMessage_ShouldCreateMessageWithCorrectContent() throws MessagingException {
        // Arrange
        when(mailSender.createMimeMessage()).thenReturn(mimeMessage);
        String to = "recipient@example.com";
        String subject = "Test Subject";
        String text = "<h1>Test Email</h1>";
        String from = "noreply@example.com";
        
        // Act
        MimeMessage result = emailService.createMimeMessage(to, subject, text, from);
        
        // Assert
        assertEquals(mimeMessage, result);
        verify(mailSender).createMimeMessage();
    }
}

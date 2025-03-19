package com.microservices.smmsb_notofication_service.service.kafka;

import com.microservices.smmsb_notofication_service.dto.kafkaEvent.NotificationEvent;
import com.microservices.smmsb_notofication_service.model.Notification;
import com.microservices.smmsb_notofication_service.repository.NotificationRepository;
import com.microservices.smmsb_notofication_service.service.impl.EmailServiceImpl;

import jakarta.mail.MessagingException;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Captor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class NotificationConsumerTest {

    @Mock
    private NotificationRepository notificationRepository;

    @Mock
    private EmailServiceImpl emailServiceImpl;

    @InjectMocks
    private NotificationConsumer notificationConsumer;

    @Captor
    private ArgumentCaptor<Notification> notificationCaptor;

    private NotificationEvent notificationEvent;

    @BeforeEach
    void setUp() {
        notificationEvent = new NotificationEvent();
        notificationEvent.setUserId(123L);
        notificationEvent.setMessage("Test notification message");
        notificationEvent.setType("INFO");
    }

    @Test
    void handleNotificationEvent_ShouldSaveNotificationAndSendEmail() throws MessagingException {
        // When
        notificationConsumer.handleNotificationEvent(notificationEvent);

        // Then
        // Verify notification is saved
        verify(notificationRepository).save(notificationCaptor.capture());
        Notification capturedNotification = notificationCaptor.getValue();
        assertEquals(notificationEvent.getUserId(), capturedNotification.getUserId());
        assertEquals(notificationEvent.getMessage(), capturedNotification.getMessage());
        assertEquals(notificationEvent.getType(), capturedNotification.getType());

        // Verify email is sent
        verify(emailServiceImpl).sendEmail(
                eq("test@example.com"),
                eq("New Notification"),
                eq(notificationEvent.getMessage())
        );
    }

    @Test
    void handleNotificationEvent_WhenExceptionOccurs_ShouldHandleGracefully() throws MessagingException {
        // Given
        doThrow(new RuntimeException("Database error")).when(notificationRepository).save(any(Notification.class));

        // When
        notificationConsumer.handleNotificationEvent(notificationEvent);

        // Then
        // Verify repository was called
        verify(notificationRepository).save(any(Notification.class));
        // Verify email service was not called due to exception
        verify(emailServiceImpl, never()).sendEmail(anyString(), anyString(), anyString());
    }

    @Test
    void handleNotificationEvent_WhenEmailServiceFails_ShouldStillSaveNotification() throws MessagingException {
        // Given
        doThrow(new RuntimeException("Email service error")).when(emailServiceImpl)
                .sendEmail(anyString(), anyString(), anyString());

        // When
        notificationConsumer.handleNotificationEvent(notificationEvent);

        // Then
        // Verify notification was still saved
        verify(notificationRepository).save(any(Notification.class));
        // Verify email service was called
        verify(emailServiceImpl).sendEmail(anyString(), anyString(), anyString());
    }
}

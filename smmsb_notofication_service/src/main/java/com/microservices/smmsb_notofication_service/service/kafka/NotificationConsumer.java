package com.microservices.smmsb_notofication_service.service.kafka;

import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Service;
import org.springframework.beans.factory.annotation.Autowired;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.transaction.annotation.Transactional;

import com.microservices.smmsb_notofication_service.dto.kafkaEvent.NotificationEvent;
import com.microservices.smmsb_notofication_service.model.Notification;
import com.microservices.smmsb_notofication_service.repository.NotificationRepository;
import com.microservices.smmsb_notofication_service.service.impl.EmailServiceImpl;

@Service
public class NotificationConsumer {
    
    private static final Logger LOGGER = LoggerFactory.getLogger(NotificationConsumer.class);
    private final NotificationRepository notificationRepository;
    private final EmailServiceImpl emailServiceImpl;

    @Autowired
    public NotificationConsumer(NotificationRepository notificationRepository, EmailServiceImpl emailServiceImpl) {
        this.notificationRepository = notificationRepository;
        this.emailServiceImpl = emailServiceImpl;
    }

    @KafkaListener(topics = "notificationTopic", groupId = "notification-group")
    @Transactional
    public void handleNotificationEvent(NotificationEvent notificationEvent) {
        try {
            LOGGER.info("Received Notification Event: {}", notificationEvent);

            Notification notification = Notification.builder()
                .userId(notificationEvent.getUserId())
                .message(notificationEvent.getMessage())
                .type(notificationEvent.getType())
                .build();

            notificationRepository.save(notification);
            LOGGER.info("Notification saved successfully for user: {}", notificationEvent.getUserId());

            // Kirim email ke MailHog
            String emailTo = "test@example.com"; // Bisa diganti dengan email user jika ada
            String subject = "New Notification";
            String message = notificationEvent.getMessage();

            emailServiceImpl.sendEmail(emailTo, subject, message);
            LOGGER.info("Notification email sent to {}", emailTo);

        } catch (Exception e) {
            LOGGER.error("Error while processing notification event", e);
        }
    }
}

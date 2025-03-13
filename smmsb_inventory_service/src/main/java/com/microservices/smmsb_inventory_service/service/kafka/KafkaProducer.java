package com.microservices.smmsb_inventory_service.service.kafka;

import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Service;

import com.microservices.smmsb_inventory_service.dto.kafkaEvent.NotificationEvent;
import com.microservices.smmsb_inventory_service.dto.kafkaEvent.LowStockAlertEvent;

import lombok.RequiredArgsConstructor;

import lombok.extern.slf4j.Slf4j;

@Slf4j
@Service
@RequiredArgsConstructor
public class KafkaProducer {
    private final KafkaTemplate<String, Object> kafkaTemplate;

    public void sendNotificationEvent(NotificationEvent event) {
        log.info("Sending event to Kafka: {}", event);
        kafkaTemplate.send("notificationTopic", event);
    }

    public void sendLowStockAlertEvent(LowStockAlertEvent event) {
        log.info("Sending event to Kafka: {}", event);
        kafkaTemplate.send("lowStockAlert", event);
    }

}
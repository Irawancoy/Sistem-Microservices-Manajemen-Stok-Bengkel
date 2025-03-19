package com.microservices.smmsb_transaction_service.service.kafka;

import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Service;

import com.microservices.smmsb_transaction_service.dto.kafkaEvent.UpdateProductStockEvent;
import com.microservices.smmsb_transaction_service.dto.kafkaEvent.NotificationEvent;

import lombok.RequiredArgsConstructor;

import lombok.extern.slf4j.Slf4j;

@Slf4j
@Service
@RequiredArgsConstructor
public class KafkaProducer {
   private final KafkaTemplate<String, Object> kafkaTemplate;

   public void sendUpdateStockEvent(UpdateProductStockEvent event) {
       log.info("Sending event to Kafka: {}", event);
       kafkaTemplate.send("update-product-stock", event);
   }

    public void sendNotificationEvent(NotificationEvent event) {
        log.info("Sending event to Kafka: {}", event); 
        kafkaTemplate.send("notificationTopic", event);
    }
}
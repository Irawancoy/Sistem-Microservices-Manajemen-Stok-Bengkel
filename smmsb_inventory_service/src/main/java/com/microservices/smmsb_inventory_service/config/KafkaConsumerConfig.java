package com.microservices.smmsb_inventory_service.config;

import org.apache.kafka.clients.consumer.ConsumerConfig;
import org.apache.kafka.common.serialization.StringDeserializer;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.kafka.config.ConcurrentKafkaListenerContainerFactory;
import org.springframework.kafka.core.ConsumerFactory;
import org.springframework.kafka.core.DefaultKafkaConsumerFactory;
import org.springframework.kafka.support.serializer.ErrorHandlingDeserializer;
import org.springframework.kafka.support.serializer.JsonDeserializer;

import com.microservices.smmsb_inventory_service.dto.kafkaEvent.UpdateProductStockEvent;

import java.util.HashMap;
import java.util.Map;

@Configuration
public class KafkaConsumerConfig {

    @Bean
    public ConsumerFactory<String, UpdateProductStockEvent> consumerFactory() {
        Map<String, Object> config = new HashMap<>();
        config.put(ConsumerConfig.BOOTSTRAP_SERVERS_CONFIG, "localhost:9082");
        config.put(ConsumerConfig.GROUP_ID_CONFIG, "inventory-group");
        config.put(ConsumerConfig.AUTO_OFFSET_RESET_CONFIG, "earliest");
        config.put(ConsumerConfig.ENABLE_AUTO_COMMIT_CONFIG, false);
        
        config.put(ConsumerConfig.KEY_DESERIALIZER_CLASS_CONFIG, ErrorHandlingDeserializer.class);
        config.put(ConsumerConfig.VALUE_DESERIALIZER_CLASS_CONFIG, ErrorHandlingDeserializer.class);
        config.put(ErrorHandlingDeserializer.KEY_DESERIALIZER_CLASS, StringDeserializer.class);
        config.put(ErrorHandlingDeserializer.VALUE_DESERIALIZER_CLASS, JsonDeserializer.class);
        
        config.put(JsonDeserializer.TRUSTED_PACKAGES, "com.microservices.smmsb_inventory_service.dto.kafkaEvent");
        config.put(JsonDeserializer.TYPE_MAPPINGS, "updateProductStock:com.microservices.smmsb_inventory_service.dto.kafkaEvent.UpdateProductStockEvent");
        config.put(JsonDeserializer.USE_TYPE_INFO_HEADERS, false);

        return new DefaultKafkaConsumerFactory<>(config, new StringDeserializer(),
                new JsonDeserializer<>(UpdateProductStockEvent.class));
    }

    @Bean
    public ConcurrentKafkaListenerContainerFactory<String, UpdateProductStockEvent> 
    kafkaListenerContainerFactory() {
        ConcurrentKafkaListenerContainerFactory<String, UpdateProductStockEvent> factory =
                new ConcurrentKafkaListenerContainerFactory<>();
        factory.setConsumerFactory(consumerFactory());
        factory.setBatchListener(false); // Sesuaikan jika ingin menggunakan batch processing
        return factory;
    }
}

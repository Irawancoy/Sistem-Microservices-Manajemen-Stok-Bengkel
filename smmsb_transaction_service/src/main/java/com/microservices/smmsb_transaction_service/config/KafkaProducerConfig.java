package com.microservices.smmsb_transaction_service.config;

import org.apache.kafka.clients.admin.NewTopic;
import org.apache.kafka.clients.admin.AdminClientConfig;
import org.apache.kafka.clients.producer.ProducerConfig;
import org.apache.kafka.common.serialization.StringSerializer;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.kafka.core.DefaultKafkaProducerFactory;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.kafka.core.ProducerFactory;
import org.springframework.kafka.support.serializer.JsonSerializer;

import java.util.HashMap;
import java.util.Map;

import org.springframework.kafka.core.KafkaAdmin;

@Configuration
public class KafkaProducerConfig {

    private static final String KAFKA_BROKER = "localhost:9082";
    private static final String TOPIC_NAME = "update-product-stock";

    // Konfigurasi Producer
    @Bean
    public ProducerFactory<String, Object> producerFactory() {
        Map<String, Object> config = new HashMap<>();
        config.put(ProducerConfig.BOOTSTRAP_SERVERS_CONFIG, KAFKA_BROKER);
        config.put(ProducerConfig.KEY_SERIALIZER_CLASS_CONFIG, StringSerializer.class);
        config.put(ProducerConfig.VALUE_SERIALIZER_CLASS_CONFIG, JsonSerializer.class);

        // Menambahkan konfigurasi untuk keandalan
        config.put(ProducerConfig.ACKS_CONFIG, "all"); // Tunggu semua replica menerima data
        config.put(ProducerConfig.RETRIES_CONFIG, 3); // Mencoba ulang jika gagal
        config.put(ProducerConfig.ENABLE_IDEMPOTENCE_CONFIG, true); // Mencegah duplikasi

        return new DefaultKafkaProducerFactory<>(config);
    }

    @Bean
    public KafkaTemplate<String, Object> kafkaTemplate() {
        return new KafkaTemplate<>(producerFactory());
    }

    // Konfigurasi Kafka Admin untuk Auto-Create Topic
    @Bean
    public KafkaAdmin kafkaAdmin() {
        Map<String, Object> configs = new HashMap<>();
        configs.put(AdminClientConfig.BOOTSTRAP_SERVERS_CONFIG, KAFKA_BROKER);
        return new KafkaAdmin(configs);
    }

    @Bean
    public NewTopic createTopic() {
        return new NewTopic(TOPIC_NAME, 1, (short) 1); // 1 partition, 1 replication factor
    }
}

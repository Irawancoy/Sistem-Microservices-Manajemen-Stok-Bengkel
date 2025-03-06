package com.microservices.smmsb_inventory_service.service.kafka;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Service;

import com.microservices.smmsb_inventory_service.dto.kafkaEvent.UpdateProductStockEvent;
import com.microservices.smmsb_inventory_service.model.ProductStock;
import com.microservices.smmsb_inventory_service.repository.ProductStockRepository;

@Service
public class KafkaConsumer {
 
    private final ProductStockRepository productStockRepository;

    @Autowired
    public KafkaConsumer(ProductStockRepository productStockRepository) {
        this.productStockRepository = productStockRepository;
    }

    @KafkaListener(topics = "update-product-stock", groupId = "inventory-group")
    public void listenUpdateStockEvent(UpdateProductStockEvent event) {
        ProductStock productStock = productStockRepository.findById(event.getProductId())
                .orElseThrow(() -> new IllegalArgumentException("Product not found"));

        // Kurangi stok produk
        productStock.setQuantity(productStock.getQuantity() - event.getQuantity());
        productStockRepository.save(productStock);
    }
}

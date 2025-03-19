package com.microservices.smmsb_inventory_service.service.impl;

import com.microservices.smmsb_inventory_service.dto.ProductStockDto;
import com.microservices.smmsb_inventory_service.dto.kafkaEvent.LowStockAlertEvent;
import com.microservices.smmsb_inventory_service.dto.kafkaEvent.NotificationEvent;
import com.microservices.smmsb_inventory_service.dto.request.CreateProductStockRequest;
import com.microservices.smmsb_inventory_service.dto.request.UpdateProductStockRequest;
import com.microservices.smmsb_inventory_service.dto.response.ApiDataResponseBuilder;
import com.microservices.smmsb_inventory_service.dto.response.ListResponse;
import com.microservices.smmsb_inventory_service.dto.response.MessageResponse;
import com.microservices.smmsb_inventory_service.model.ProductStock;
import com.microservices.smmsb_inventory_service.repository.ProductStockRepository;
import com.microservices.smmsb_inventory_service.service.MinioService;
import com.microservices.smmsb_inventory_service.service.ProductStockService;
import com.microservices.smmsb_inventory_service.service.kafka.KafkaProducer;
import com.microservices.smmsb_inventory_service.utils.MessageUtils;
import com.microservices.smmsb_inventory_service.utils.ProductStockSpesification;

import jakarta.servlet.http.HttpServletRequest;

import org.apache.kafka.common.errors.ResourceNotFoundException;
import org.modelmapper.ModelMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.data.jpa.domain.Specification;

import java.math.BigDecimal;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import lombok.extern.slf4j.Slf4j;

@Slf4j
@Service
public class ProductStockServiceImpl implements ProductStockService {

        private static final int LOW_STOCK_THRESHOLD = 10;

        private final ProductStockRepository productStockRepository;
        private final ModelMapper modelMapper;
        private final MessageUtils messageUtils;
        private final MinioService minioService;
        private final KafkaProducer kafkaProducer;

        @Autowired
        public ProductStockServiceImpl(ProductStockRepository productStockRepository, ModelMapper modelMapper,
                        MessageUtils messageUtils, MinioService minioService, KafkaProducer kafkaProducer) {
                this.productStockRepository = productStockRepository;
                this.modelMapper = modelMapper;
                this.messageUtils = messageUtils;
                this.minioService = minioService;
                this.kafkaProducer = kafkaProducer;

        }

        @Override
        @Transactional
        public MessageResponse createProductStock(CreateProductStockRequest createProductStockRequest,
                        HttpServletRequest request) {
                String userIdHeader = request.getHeader("X-User-Id");
                if (userIdHeader == null) {
                        return new MessageResponse(
                                        messageUtils.getMessage("user.not.found"),
                                        HttpStatus.UNAUTHORIZED.value(),
                                        HttpStatus.UNAUTHORIZED.name());
                }

                Long userId;
                try {
                        userId = Long.parseLong(userIdHeader);
                } catch (NumberFormatException e) {
                        return new MessageResponse(
                                        messageUtils.getMessage("invalid.user.id"),
                                        HttpStatus.BAD_REQUEST.value(),
                                        HttpStatus.BAD_REQUEST.name());
                }

                ProductStock productStock = modelMapper.map(createProductStockRequest, ProductStock.class);

                productStock.setCreatedBy(userId);

                productStock = productStockRepository.save(productStock);
                if (productStock.getId() == null) {
                        return new MessageResponse(
                                        messageUtils.getMessage("product.creation.failed"),
                                        HttpStatus.INTERNAL_SERVER_ERROR.value(),
                                        HttpStatus.INTERNAL_SERVER_ERROR.name());
                }

                NotificationEvent notificationEvent = new NotificationEvent(
                                userId,
                                "Produk " + createProductStockRequest.getProductName() + " berhasil ditambahkan",
                                "Create");

                kafkaProducer.sendNotificationEvent(notificationEvent);

                return new MessageResponse(
                                messageUtils.getMessage("product.created", productStock.getProductName()),
                                HttpStatus.CREATED.value(),
                                HttpStatus.CREATED.name());
        }

        @Override
        @Transactional
        public MessageResponse updateProductStock(Long id, UpdateProductStockRequest updateProductStockRequest,
                        HttpServletRequest request) {
                String userIdHeader = request.getHeader("X-User-Id");
                if (userIdHeader == null) {
                        return new MessageResponse(
                                        messageUtils.getMessage("user.not.found"),
                                        HttpStatus.UNAUTHORIZED.value(),
                                        HttpStatus.UNAUTHORIZED.name());
                }

                Long userId;
                try {
                        userId = Long.parseLong(userIdHeader);
                } catch (NumberFormatException e) {
                        return new MessageResponse(
                                        messageUtils.getMessage("invalid.user.id"),
                                        HttpStatus.BAD_REQUEST.value(),
                                        HttpStatus.BAD_REQUEST.name());
                }
                ProductStock productStock = productStockRepository.findById(id)
                                .orElseThrow(() -> new IllegalArgumentException("Product not found with id: " + id));
                modelMapper.map(updateProductStockRequest, productStock);
                productStockRepository.save(productStock);

                /// Send a Kafka event to notify the notification service
                NotificationEvent notificationEvent = new NotificationEvent(
                                userId,
                                "Produk " + updateProductStockRequest.getProductName() + " berhasil diupdate",
                                "Update");
                kafkaProducer.sendNotificationEvent(notificationEvent);
                return new MessageResponse(
                                messageUtils.getMessage("product.updated", productStock.getProductName()),
                                HttpStatus.OK.value(),
                                HttpStatus.OK.name());
        }

        @Override
        public ApiDataResponseBuilder getProductStockById(Long id) {
                ProductStock productStock = productStockRepository.findById(id)
                                .orElseThrow(() -> new IllegalArgumentException("Product not found with id: " + id));
                ProductStockDto productStockDto = modelMapper.map(productStock, ProductStockDto.class);
                return ApiDataResponseBuilder.builder()
                                .data(productStockDto)
                                .message(messageUtils.getMessage("products.found", productStock.getProductName()))
                                .status(HttpStatus.OK)
                                .statusCode(HttpStatus.OK.value())
                                .build();
        }

        @Override
        @Transactional
        public MessageResponse deleteProductStock(Long id, HttpServletRequest request) {
                String userIdHeader = request.getHeader("X-User-Id");
                if (userIdHeader == null) {
                        return new MessageResponse(
                                        messageUtils.getMessage("user.not.found"),
                                        HttpStatus.UNAUTHORIZED.value(),
                                        HttpStatus.UNAUTHORIZED.name());
                }

                Long userId;
                try {
                        userId = Long.parseLong(userIdHeader);
                } catch (NumberFormatException e) {
                        return new MessageResponse(
                                        messageUtils.getMessage("invalid.user.id"),
                                        HttpStatus.BAD_REQUEST.value(),
                                        HttpStatus.BAD_REQUEST.name());
                }
                ProductStock productStock = productStockRepository.findById(id)
                                .orElseThrow(() -> new IllegalArgumentException("Product not found with id: " + id));
                productStockRepository.delete(productStock);
                /// Send a Kafka event to notify the notification service
                NotificationEvent notificationEvent = new NotificationEvent(
                                userId,
                                "Produk " + productStock.getProductName() + " berhasil dihapus",
                                "Delete");
                kafkaProducer.sendNotificationEvent(notificationEvent);

                return new MessageResponse(
                                messageUtils.getMessage("product.deleted", productStock.getProductName()),
                                HttpStatus.OK.value(),
                                HttpStatus.OK.name());
        }

        @Override
        @Transactional
        public ListResponse<Map<String, String>> uploadImage(MultipartFile file, Long Id) {
                ProductStock product = productStockRepository
                                .findById(Id)
                                .orElseThrow(
                                                () -> new ResourceNotFoundException(
                                                                messageUtils.getMessage("product.notFound", Id)));
                if (product.getImageUrl() != null) {
                        try {
                                minioService.removeObject("products", getFileNameFromUrl(product.getImageUrl()));
                        } catch (Exception e) {
                                // Log error
                        }
                }

                String objectName = "products/" + Id + "_" + System.currentTimeMillis() + "_"
                                + file.getOriginalFilename();
                String url = minioService.uploadFile(file, objectName, "smmsbproducts");
                product.setImageUrl(url);
                productStockRepository.save(product);

                Map<String, String> urlMap = new HashMap<>();
                urlMap.put("url", url);
                ListResponse<Map<String, String>> response = new ListResponse<>(
                                Collections.singletonList(urlMap),
                                messageUtils.getMessage("productStock.uploadPhoto.success", product.getProductName()),
                                HttpStatus.OK.value(),
                                HttpStatus.OK.name());
                return response;
        }

        @Override
        public ListResponse<ProductStockDto> getAllProductStocks(Pageable pageable, String productName,
                        BigDecimal price) {
                Specification<ProductStock> spec = Specification.where(null);
                if (productName != null) {
                        spec = spec.and(ProductStockSpesification.hasProductName(productName));
                }
                if (price != null) {
                        spec = spec.and(ProductStockSpesification.hasPrice(price));
                }

                Page<ProductStock> productStocks = productStockRepository.findAll(spec, pageable);
                List<ProductStockDto> productStockDtos = productStocks.getContent().stream()
                                .map(product -> modelMapper.map(product, ProductStockDto.class))
                                .collect(Collectors.toList());

                return new ListResponse<>(productStockDtos, messageUtils.getMessage("products.found"),
                                HttpStatus.OK.value(), HttpStatus.OK.name());
        }

        private String getFileNameFromUrl(String url) {
                return url.substring(url.lastIndexOf("/") + 1);
        }

        @Override
        @Scheduled(fixedRate = 3600000) // 1 jam
        public void checkAndNotifyLowStockProducts() {
            List<ProductStock> lowStockProducts = productStockRepository
                    .findByQuantityLessThan(LOW_STOCK_THRESHOLD);
        
            for (ProductStock product : lowStockProducts) {
                // Membuat event low stock
                LowStockAlertEvent lowStockAlertEvent = new LowStockAlertEvent(
                        product.getId(),
                        product.getQuantity(),
                        product.getProductName(),
                        "Stok produk " + product.getProductName() + " kurang dari " + LOW_STOCK_THRESHOLD
                );
        
                // Kirim event ke Kafka untuk low stock alert
                kafkaProducer.sendLowStockAlertEvent(lowStockAlertEvent);
                log.info("Low stock alert event sent for product: {}", product.getProductName());
        
                // Membuat event notifikasi untuk user (misalnya admin gudang)
                NotificationEvent notificationEvent = new NotificationEvent(
                        null, // userId bisa di-set null jika tidak spesifik ke user tertentu
                        lowStockAlertEvent.getMessage(), // Ambil message dari event low stock alert
                        "LOW_STOCK_ALERT"
                );
        
                // Kirim event ke Kafka untuk notifikasi
                kafkaProducer.sendNotificationEvent(notificationEvent);
                log.info("Notification event sent for low stock product: {}", product.getProductName());
            }
        }
        

}
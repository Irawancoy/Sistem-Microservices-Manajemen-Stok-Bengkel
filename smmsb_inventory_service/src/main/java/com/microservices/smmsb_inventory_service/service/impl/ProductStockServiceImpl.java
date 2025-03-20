package com.microservices.smmsb_inventory_service.service.impl;

import com.microservices.smmsb_inventory_service.dto.ProductStockDto;
import com.microservices.smmsb_inventory_service.dto.kafkaEvent.LowStockAlertEvent;
import com.microservices.smmsb_inventory_service.dto.kafkaEvent.NotificationEvent;
import com.microservices.smmsb_inventory_service.dto.request.CreateProductStockRequest;
import com.microservices.smmsb_inventory_service.dto.request.UpdateProductStockRequest;
import com.microservices.smmsb_inventory_service.dto.response.ApiDataResponseBuilder;
import com.microservices.smmsb_inventory_service.dto.response.ListResponse;
import com.microservices.smmsb_inventory_service.dto.response.MessageResponse;
import com.microservices.smmsb_inventory_service.exception.AuthenticationFailedException;
import com.microservices.smmsb_inventory_service.exception.BadRequestException;
import com.microservices.smmsb_inventory_service.exception.DuplicateResourceException;
import com.microservices.smmsb_inventory_service.exception.ResourceNotFoundException;
import com.microservices.smmsb_inventory_service.model.ProductStock;
import com.microservices.smmsb_inventory_service.repository.ProductStockRepository;
import com.microservices.smmsb_inventory_service.service.MinioService;
import com.microservices.smmsb_inventory_service.service.ProductStockService;
import com.microservices.smmsb_inventory_service.service.kafka.KafkaProducer;
import com.microservices.smmsb_inventory_service.utils.MessageUtils;
import com.microservices.smmsb_inventory_service.utils.ProductStockSpesification;

import jakarta.servlet.http.HttpServletRequest;

import org.modelmapper.ModelMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.data.jpa.domain.Specification;

import java.math.BigDecimal;
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
        public MessageResponse createProductStock(CreateProductStockRequest createProductStockRequest) {
                HttpServletRequest request = ((ServletRequestAttributes) RequestContextHolder.getRequestAttributes())
                                .getRequest();
                String userIdHeader = request.getHeader("X-User-Id");
                if (userIdHeader == null) {
                        throw new AuthenticationFailedException(messageUtils.getMessage("error.user.not.found"));
                }

                Long userId;
                try {
                        userId = Long.parseLong(userIdHeader);
                } catch (NumberFormatException e) {
                        throw new BadRequestException(messageUtils.getMessage("error.invalid.user.id"));
                }

                // Check if productName already exists
                if (productStockRepository
                                .existsByProductNameAndIsDeletedFalse(createProductStockRequest.getProductName())) {
                        throw new DuplicateResourceException(
                                        messageUtils.getMessage("error.product.name.already.exists",
                                                        createProductStockRequest.getProductName()));
                }

                ProductStock productStock = modelMapper.map(createProductStockRequest, ProductStock.class);
                productStock.setCreatedBy(userId);
                productStock = productStockRepository.save(productStock);

                NotificationEvent notificationEvent = new NotificationEvent(
                                userId,
                                "Produk " + createProductStockRequest.getProductName() + " berhasil ditambahkan",
                                "Create");
                kafkaProducer.sendNotificationEvent(notificationEvent);

                return new MessageResponse(
                                messageUtils.getMessage("success.product.created", productStock.getProductName()),
                                HttpStatus.CREATED.value(),
                                HttpStatus.CREATED.name());
        }

        @Override
        @Transactional
        public MessageResponse updateProductStock(Long id, UpdateProductStockRequest updateProductStockRequest) {
                HttpServletRequest request = ((ServletRequestAttributes) RequestContextHolder.getRequestAttributes())
                                .getRequest();
                String userIdHeader = request.getHeader("X-User-Id");

                if (userIdHeader == null) {
                        throw new AuthenticationFailedException(messageUtils.getMessage("error.user.not.found"));
                }

                Long userId;
                try {
                        userId = Long.parseLong(userIdHeader);
                } catch (NumberFormatException e) {
                        throw new BadRequestException(messageUtils.getMessage("error.invalid.user.id"));
                }

                // Cek apakah produk ada di database
                ProductStock productStock = productStockRepository.findByIdAndIsDeletedFalse(id)
                                .orElseThrow(() -> new ResourceNotFoundException(
                                                messageUtils.getMessage("error.product.not.found")));

                // Jika productName dikirim dalam request, cek apakah sudah ada di database
                if (updateProductStockRequest.getProductName() != null
                                && !updateProductStockRequest.getProductName().equals(productStock.getProductName())) {

                        if (productStockRepository.existsByProductNameAndIsDeletedFalse(
                                        updateProductStockRequest.getProductName())) {
                                throw new DuplicateResourceException(
                                                messageUtils.getMessage("error.product.name.already.exists",
                                                                updateProductStockRequest.getProductName()));
                        }
                        productStock.setProductName(updateProductStockRequest.getProductName());
                }

                // Update description jika ada di request
                if (updateProductStockRequest.getDescription() != null) {
                        productStock.setDescription(updateProductStockRequest.getDescription());
                }

                // Update quantity jika ada di request
                if (updateProductStockRequest.getQuantity() > 0) {
                        productStock.setQuantity(updateProductStockRequest.getQuantity());
                }

                // Update price jika ada di request
                if (updateProductStockRequest.getPrice() != null
                                && updateProductStockRequest.getPrice().compareTo(BigDecimal.ZERO) > 0) {
                        productStock.setPrice(updateProductStockRequest.getPrice());
                }

                // Update updatedBy
                productStock.setUpdatedBy(userId);

                // Simpan perubahan
                productStockRepository.save(productStock);

                // Kirim event Kafka untuk memberi tahu layanan notifikasi
                NotificationEvent notificationEvent = new NotificationEvent(
                                userId,
                                "Produk " + productStock.getProductName() + " berhasil diupdate",
                                "Update");
                kafkaProducer.sendNotificationEvent(notificationEvent);

                return new MessageResponse(
                                messageUtils.getMessage("success.product.updated", productStock.getProductName()),
                                HttpStatus.OK.value(),
                                HttpStatus.OK.name());
        }

        @Override
        public ApiDataResponseBuilder getProductStockById(Long id) {
                ProductStock productStock = productStockRepository.findByIdAndIsDeletedFalse(id)
                                .orElseThrow(() -> new ResourceNotFoundException(
                                                messageUtils.getMessage("error.product.not.found")));

                ProductStockDto productStockDto = modelMapper.map(productStock, ProductStockDto.class);

                return ApiDataResponseBuilder.builder()
                                .data(productStockDto)
                                .message(messageUtils.getMessage("success.product.retrieved"))
                                .status(HttpStatus.OK)
                                .statusCode(HttpStatus.OK.value())
                                .build();
        }

        @Override
        @Transactional
        public MessageResponse deleteProductStock(Long id) {
                HttpServletRequest request = ((ServletRequestAttributes) RequestContextHolder.getRequestAttributes())
                                .getRequest();
                String userIdHeader = request.getHeader("X-User-Id");
                if (userIdHeader == null) {
                        throw new AuthenticationFailedException(messageUtils.getMessage("error.user.not.found"));
                }

                Long userId;
                try {
                        userId = Long.parseLong(userIdHeader);
                } catch (NumberFormatException e) {
                        throw new BadRequestException(messageUtils.getMessage("error.invalid.user.id"));
                }

                ProductStock productStock = productStockRepository.findByIdAndIsDeletedFalse(id)
                                .orElseThrow(() -> new ResourceNotFoundException(
                                                messageUtils.getMessage("error.product.not.found")));

                // Soft delete the product stock
                productStock.setDeleted(true);
                productStock.setDeletedBy(userId);

                productStockRepository.save(productStock);

                /// Send a Kafka event to notify the notification service
                NotificationEvent notificationEvent = new NotificationEvent(
                                userId,
                                "Produk " + productStock.getProductName() + " berhasil dihapus",
                                "Delete");
                kafkaProducer.sendNotificationEvent(notificationEvent);

                return new MessageResponse(
                                messageUtils.getMessage("success.product.deleted", productStock.getProductName()),
                                HttpStatus.OK.value(),
                                HttpStatus.OK.name());
        }

        @Override
        @Transactional
        public ListResponse<Map<String, String>> uploadImage(MultipartFile file, Long id) {
                ProductStock product = productStockRepository.findByIdAndIsDeletedFalse(id)
                                .orElseThrow(() -> new ResourceNotFoundException(
                                                messageUtils.getMessage("error.product.not.found")));

                // 🔹 Validasi format file yang diperbolehkan
                String contentType = file.getContentType();
                List<String> allowedTypes = List.of("image/jpeg", "image/jpg", "image/png");

                if (contentType == null || !allowedTypes.contains(contentType)) {
                        throw new BadRequestException(messageUtils.getMessage("error.invalid.file.type"));
                }

                // 🔹 Hapus gambar lama jika ada
                if (product.getImageUrl() != null && !product.getImageUrl().isEmpty()) {
                        String oldFileName = getFileNameFromUrl(product.getImageUrl());
                        try {
                                minioService.removeObject("smmsbproducts", "products/" + oldFileName);
                                log.info("Successfully deleted old image: {}", oldFileName);
                        } catch (Exception e) {
                                log.error("Failed to delete old image: {} - Error: {}", oldFileName, e.getMessage());
                        }
                }

                // 🔹 Upload gambar baru
                String objectName = String.format("products/%d_%d_%s", id, System.currentTimeMillis(),
                                file.getOriginalFilename());
                String imageUrl = minioService.uploadFile(file, objectName, "smmsbproducts");

                // 🔹 Simpan URL ke database
                product.setImageUrl(imageUrl);
                productStockRepository.save(product);
                log.info("Product updated successfully with new image: {}", imageUrl);

                // 🔹 Build response
                Map<String, String> responseData = Map.of("url", imageUrl);
                return new ListResponse<>(
                                List.of(responseData),
                                messageUtils.getMessage("success.product.upload.photo", product.getProductName()),
                                HttpStatus.OK.value(),
                                HttpStatus.OK.name());
        }

        @Override
        public ListResponse<ProductStockDto> getAllProductStocks(Pageable pageable, String productName,
                        BigDecimal price,
                                        Integer quantity) {
                Specification<ProductStock> spec = Specification.where(null);
                if (productName != null) {
                        spec = spec.and(ProductStockSpesification.hasProductName(productName));
                }
                if (price != null) {
                        spec = spec.and(ProductStockSpesification.hasPrice(price));
                }
                if (quantity != null) {
                        spec = spec.and(ProductStockSpesification.hasQuantity(quantity));
                }

                Page<ProductStock> productStocks = productStockRepository.findAll(spec, pageable);
                List<ProductStockDto> productStockDtos = productStocks.getContent().stream()
                                .map(product -> modelMapper.map(product, ProductStockDto.class))
                                .collect(Collectors.toList());

                return new ListResponse<>(productStockDtos, messageUtils.getMessage("success.product.retrieved"),
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
                                        "Stok produk " + product.getProductName() + " kurang dari "
                                                        + LOW_STOCK_THRESHOLD);

                        // Kirim event ke Kafka untuk low stock alert
                        kafkaProducer.sendLowStockAlertEvent(lowStockAlertEvent);
                        log.info("Low stock alert event sent for product: {}", product.getProductName());

                        // Membuat event notifikasi untuk user (misalnya admin gudang)
                        NotificationEvent notificationEvent = new NotificationEvent(
                                        null, // userId bisa di-set null jika tidak spesifik ke user tertentu
                                        lowStockAlertEvent.getMessage(), // Ambil message dari event low stock alert
                                        "LOW_STOCK_ALERT");

                        // Kirim event ke Kafka untuk notifikasi
                        kafkaProducer.sendNotificationEvent(notificationEvent);
                        log.info("Notification event sent for low stock product: {}", product.getProductName());
                }
        }

}
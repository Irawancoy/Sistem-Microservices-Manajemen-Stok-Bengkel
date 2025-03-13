package com.microservices.smmsb_transaction_service.service.impl;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.client.RestTemplate;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;

import com.microservices.smmsb_transaction_service.dto.TransactionDto;
import com.microservices.smmsb_transaction_service.dto.kafkaEvent.UpdateProductStockEvent;
import com.microservices.smmsb_transaction_service.dto.kafkaEvent.NotificationEvent;
import com.microservices.smmsb_transaction_service.dto.request.CreateTransactionRequest;
import com.microservices.smmsb_transaction_service.dto.response.ApiDataResponseBuilder;
import com.microservices.smmsb_transaction_service.dto.response.ListResponse;
import com.microservices.smmsb_transaction_service.dto.response.MessageResponse;
import com.microservices.smmsb_transaction_service.model.Transaction;
import com.microservices.smmsb_transaction_service.repository.TransactionRepository;
import com.microservices.smmsb_transaction_service.service.TransactionService;
import com.microservices.smmsb_transaction_service.service.kafka.KafkaProducer;
import com.microservices.smmsb_transaction_service.utils.MessageUtils;
import com.microservices.smmsb_transaction_service.utils.TransactionSpesification;

import jakarta.servlet.http.HttpServletRequest;

import org.modelmapper.ModelMapper;
import org.springframework.data.jpa.domain.Specification;

import java.math.BigDecimal;
import java.util.List;
import java.util.stream.Collectors;
import java.util.Map;

@Service
public class TransactionServiceImpl implements TransactionService {

   private final ModelMapper modelMapper;
   private final TransactionRepository transactionRepository;
   private final MessageUtils messageUtils;
   private final RestTemplate restTemplate;
   private final KafkaProducer kafkaProducer;

   @Autowired
   public TransactionServiceImpl(ModelMapper modelMapper, TransactionRepository transactionRepository,
         MessageUtils messageUtils, RestTemplate restTemplate, KafkaProducer kafkaProducer) {

      this.modelMapper = modelMapper;
      this.transactionRepository = transactionRepository;
      this.messageUtils = messageUtils;
      this.restTemplate = restTemplate;
      this.kafkaProducer = kafkaProducer;
   }

   @Override
   @Transactional
   public MessageResponse createTransaction(CreateTransactionRequest createTransactionRequest,
         HttpServletRequest httpServletRequest) {

      // Mengambil User Id
      String userIdHeader = httpServletRequest.getHeader("X-User-Id");
      if (userIdHeader == null) {
         return new MessageResponse(
               messageUtils.getMessage("user.not.found"),
               HttpStatus.UNAUTHORIZED.value(),
               HttpStatus.UNAUTHORIZED.name()

         );
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

      // Membuat objek transaksi
      Transaction transaction = new Transaction();
      transaction.setProductId(createTransactionRequest.getProductId());
      transaction.setUserId(userId);
      transaction.setQuantity(createTransactionRequest.getQuantity());
      // URL untuk memanggil Inventory Service
      String inventoryUrl = "http://localhost:8080/api/v1/product-stock/get-by-id/"
            + createTransactionRequest.getProductId();
      
      HttpHeaders headers = new HttpHeaders();
      headers.set("Authorization", "Bearer " + httpServletRequest.getHeader("Authorization"));
      headers.set("X-Session-Id", httpServletRequest.getHeader("X-Session-Id"));

      HttpEntity<String> requestEntity = new HttpEntity<>(headers);

      // Panggil Inventory Service menggunakan RestTemplate
      @SuppressWarnings("rawtypes")
      ResponseEntity<Map> response = restTemplate.exchange(inventoryUrl,HttpMethod.GET,requestEntity,Map.class);

      // Periksa jika respons sukses
      if (response.getStatusCode().is2xxSuccessful() && response.getBody() != null) {
         @SuppressWarnings("unchecked")
         Map<String, Object> responseData = (Map<String, Object>) response.getBody().get("data");

         if (responseData != null) {
            // Ambil data produk dari response
            String productName = (String) responseData.get("productName");
            BigDecimal price = new BigDecimal(responseData.get("price").toString());

            // Set nilai ke transaksi
            transaction.setProductName(productName);
            transaction.setPrice(price);
            transaction.setTotalAmount(price.multiply(BigDecimal.valueOf(transaction.getQuantity())));
         } else {
            throw new RuntimeException("Product data not found in Inventory Service response");
         }
      } else {
         throw new RuntimeException("Failed to fetch product details from Inventory Service");
      }

      // Simpan transaksi ke database
      transaction = transactionRepository.save(transaction);

      // Kirim pesan ke Kafka
      UpdateProductStockEvent event = new UpdateProductStockEvent(transaction.getProductId(),
            transaction.getQuantity());
      kafkaProducer.sendUpdateStockEvent(event);

      // Kirim pesan ke Kafka untuk notifikasi
      NotificationEvent notificationEvent = new NotificationEvent(
            transaction.getUserId(),
            "Transaksi berhasil untuk produk: " + transaction.getProductName() + " sejumlah: "
                  + transaction.getQuantity(),
            "Email");
      kafkaProducer.sendNotificationEvent(notificationEvent);
      // Return response
      return new MessageResponse(
            messageUtils.getMessage("transaction.created", transaction.getId()),
            HttpStatus.CREATED.value(),
            HttpStatus.CREATED.name());
   }

   @Override
   public ApiDataResponseBuilder getTransactionById(Long transactionId) {
      Transaction transaction = transactionRepository.findById(transactionId)
            .orElseThrow(() -> new IllegalArgumentException("Transaction not found with id: " + transactionId));
      TransactionDto transactionDto = modelMapper.map(transaction, TransactionDto.class);
      return ApiDataResponseBuilder.builder()
            .data(transactionDto)
            .message(messageUtils.getMessage("transactions.found", transaction.getId()))
            .status(HttpStatus.OK)
            .statusCode(HttpStatus.OK.value())
            .build();
   }

   @Override
   public ListResponse<TransactionDto> getAllTransactions(Pageable pageable, String productName, String status,
         Integer quantity) {
      Specification<Transaction> spec = Specification.where(null);

      if (productName != null) {
         spec = spec.and(TransactionSpesification.filterProductName(productName));
      }
      if (status != null) {
         spec = spec.and(TransactionSpesification.filterByStatus(status));
      }
      if (quantity != null) {
         spec = spec.and(TransactionSpesification.filterByQuantity(quantity));
      }

      Page<Transaction> transactions = transactionRepository.findAll(spec, pageable);
      List<TransactionDto> transactionDtos = transactions.getContent()
            .stream()
            .map(transaction -> modelMapper.map(transaction, TransactionDto.class))
            .collect(Collectors.toList());

      return new ListResponse<>(transactionDtos, messageUtils.getMessage("transactions.found"), HttpStatus.OK.value(),
            HttpStatus.OK.name());
   }

}
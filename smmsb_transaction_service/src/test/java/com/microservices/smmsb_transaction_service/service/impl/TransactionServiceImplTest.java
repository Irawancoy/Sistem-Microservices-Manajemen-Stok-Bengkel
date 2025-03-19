package com.microservices.smmsb_transaction_service.service.impl;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

import java.math.BigDecimal;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.modelmapper.ModelMapper;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.client.RestTemplate;

import com.microservices.smmsb_transaction_service.dto.TransactionDto;
import com.microservices.smmsb_transaction_service.dto.kafkaEvent.NotificationEvent;
import com.microservices.smmsb_transaction_service.dto.kafkaEvent.UpdateProductStockEvent;
import com.microservices.smmsb_transaction_service.dto.request.CreateTransactionRequest;
import com.microservices.smmsb_transaction_service.dto.response.ApiDataResponseBuilder;
import com.microservices.smmsb_transaction_service.dto.response.ListResponse;
import com.microservices.smmsb_transaction_service.dto.response.MessageResponse;
import com.microservices.smmsb_transaction_service.model.Transaction;
import com.microservices.smmsb_transaction_service.repository.TransactionRepository;
import com.microservices.smmsb_transaction_service.service.kafka.KafkaProducer;
import com.microservices.smmsb_transaction_service.utils.MessageUtils;

import jakarta.servlet.http.HttpServletRequest;

@ExtendWith(MockitoExtension.class)
public class TransactionServiceImplTest {

    @Mock
    private ModelMapper modelMapper;

    @Mock
    private TransactionRepository transactionRepository;

    @Mock
    private MessageUtils messageUtils;

    @Mock
    private RestTemplate restTemplate;

    @Mock
    private KafkaProducer kafkaProducer;

    @Mock
    private HttpServletRequest httpServletRequest;

    @InjectMocks
    private TransactionServiceImpl transactionService;

    private Transaction transaction;
    private TransactionDto transactionDto;
    private CreateTransactionRequest createTransactionRequest;

    @BeforeEach
    void setUp() {
        // Setup test data
        transaction = new Transaction();
        transaction.setId(1L);
        transaction.setProductId(100L);
        transaction.setProductName("Test Product");
        transaction.setUserId(200L);
        transaction.setQuantity(5);
        transaction.setPrice(new BigDecimal("100.00"));
        transaction.setTotalAmount(new BigDecimal("500.00"));

        transactionDto = new TransactionDto();
        transactionDto.setId(1L);
        transactionDto.setProductId(100L);
        transactionDto.setProductName("Test Product");
        transactionDto.setUserId(200L);
        transactionDto.setQuantity(5);
        transactionDto.setPrice(new BigDecimal("100.00"));
        transactionDto.setTotalAmount(new BigDecimal("500.00"));

        createTransactionRequest = new CreateTransactionRequest();
        createTransactionRequest.setProductId(100L);
        createTransactionRequest.setQuantity(5);
    }

    @SuppressWarnings("rawtypes")
   @Test
    void createTransaction_Success() {
        // Arrange
        when(httpServletRequest.getHeader("X-User-Id")).thenReturn("200");
        when(httpServletRequest.getHeader("Authorization")).thenReturn("Bearer token");
        when(httpServletRequest.getHeader("X-Session-Id")).thenReturn("session-id");

        Map<String, Object> productData = new HashMap<>();
        productData.put("productName", "Test Product");
        productData.put("price", 100.00);

        Map<String, Object> responseBody = new HashMap<>();
        responseBody.put("data", productData);

        ResponseEntity<Map> responseEntity = new ResponseEntity<>(responseBody, HttpStatus.OK);
        when(restTemplate.exchange(anyString(), eq(HttpMethod.GET), any(HttpEntity.class), eq(Map.class)))
                .thenReturn(responseEntity);

        when(transactionRepository.save(any(Transaction.class))).thenReturn(transaction);
        when(messageUtils.getMessage("transaction.created", 1L)).thenReturn("Transaction created successfully with ID: 1");

        // Act
        MessageResponse response = transactionService.createTransaction(createTransactionRequest, httpServletRequest);

        // Assert
        assertNotNull(response);
        assertEquals(HttpStatus.CREATED.value(), response.getStatusCode());
        assertEquals(HttpStatus.CREATED.name(), response.getStatus());
        
        // Verify interactions
        verify(kafkaProducer).sendUpdateStockEvent(any(UpdateProductStockEvent.class));
        verify(kafkaProducer).sendNotificationEvent(any(NotificationEvent.class));
        verify(transactionRepository).save(any(Transaction.class));
    }

    @Test
    void createTransaction_MissingUserId() {
        // Arrange
        when(httpServletRequest.getHeader("X-User-Id")).thenReturn(null);
        when(messageUtils.getMessage("user.not.found")).thenReturn("User not found");

        // Act
        MessageResponse response = transactionService.createTransaction(createTransactionRequest, httpServletRequest);

        // Assert
        assertNotNull(response);
        assertEquals(HttpStatus.UNAUTHORIZED.value(), response.getStatusCode());
        assertEquals(HttpStatus.UNAUTHORIZED.name(), response.getStatus());
        
        // Verify no interactions with repository or kafka
        verifyNoInteractions(transactionRepository, kafkaProducer);
    }

    @Test
    void createTransaction_InvalidUserId() {
        // Arrange
        when(httpServletRequest.getHeader("X-User-Id")).thenReturn("invalid-id");
        when(messageUtils.getMessage("invalid.user.id")).thenReturn("Invalid user ID");

        // Act
        MessageResponse response = transactionService.createTransaction(createTransactionRequest, httpServletRequest);

        // Assert
        assertNotNull(response);
        assertEquals(HttpStatus.BAD_REQUEST.value(), response.getStatusCode());
        assertEquals(HttpStatus.BAD_REQUEST.name(), response.getStatus());
        
        // Verify no interactions with repository or kafka
        verifyNoInteractions(transactionRepository, kafkaProducer);
    }

    @Test
    void getTransactionById_Success() {
        // Arrange
        when(transactionRepository.findById(1L)).thenReturn(Optional.of(transaction));
        when(modelMapper.map(transaction, TransactionDto.class)).thenReturn(transactionDto);
        when(messageUtils.getMessage("transactions.found", 1L)).thenReturn("Transaction found with ID: 1");

        // Act
        ApiDataResponseBuilder response = transactionService.getTransactionById(1L);

        // Assert
        assertNotNull(response);
        assertEquals(HttpStatus.OK, response.getStatus());
        assertEquals(HttpStatus.OK.value(), response.getStatusCode());
        assertEquals(transactionDto, response.getData());
    }

    @Test
    void getTransactionById_NotFound() {
        // Arrange
        when(transactionRepository.findById(999L)).thenReturn(Optional.empty());

        // Act & Assert
        assertThrows(IllegalArgumentException.class, () -> {
            transactionService.getTransactionById(999L);
        });
    }

    @SuppressWarnings("unchecked")
   @Test
    void getAllTransactions_Success() {
        // Arrange
        Pageable pageable = PageRequest.of(0, 10);
        Page<Transaction> transactionPage = new PageImpl<>(Arrays.asList(transaction));
        
        when(transactionRepository.findAll(any(Specification.class), eq(pageable))).thenReturn(transactionPage);
        when(modelMapper.map(transaction, TransactionDto.class)).thenReturn(transactionDto);
        when(messageUtils.getMessage("transactions.found")).thenReturn("Transactions found");

        // Act
        ListResponse<TransactionDto> response = transactionService.getAllTransactions(pageable, "Test", "ACTIVE", 5);

        // Assert
        assertNotNull(response);
        assertEquals(HttpStatus.OK.value(), response.getStatusCode());
        assertEquals(HttpStatus.OK.name(), response.getStatus());
        assertEquals(1, response.getData().size());
        assertEquals(transactionDto, response.getData().get(0));
    }
}

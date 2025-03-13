package com.microservices.smmsb_transaction_service.service;

import com.microservices.smmsb_transaction_service.dto.response.MessageResponse;

import jakarta.servlet.http.HttpServletRequest;

import com.microservices.smmsb_transaction_service.dto.response.ListResponse;

import org.springframework.data.domain.Pageable;
import com.microservices.smmsb_transaction_service.dto.TransactionDto;
import com.microservices.smmsb_transaction_service.dto.request.CreateTransactionRequest;
import com.microservices.smmsb_transaction_service.dto.response.ApiDataResponseBuilder;

public interface TransactionService {
   MessageResponse createTransaction(CreateTransactionRequest createTransactionRequest,HttpServletRequest httpServletRequest);

   ApiDataResponseBuilder getTransactionById(Long transactionId);

   ListResponse<TransactionDto> getAllTransactions(
         Pageable pageable,
         String productName,
         String status,
         Integer quantity
   );
   

}
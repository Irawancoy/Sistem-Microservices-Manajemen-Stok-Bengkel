package com.microservices.smmsb_transaction_service.controller;

import com.microservices.smmsb_transaction_service.dto.TransactionDto;
import com.microservices.smmsb_transaction_service.dto.request.CreateTransactionRequest;
import com.microservices.smmsb_transaction_service.dto.response.ApiDataResponseBuilder;
import com.microservices.smmsb_transaction_service.dto.response.ListResponse;
import com.microservices.smmsb_transaction_service.dto.response.MessageResponse;
import com.microservices.smmsb_transaction_service.service.TransactionService;

import jakarta.servlet.http.HttpServletRequest;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1/transactions")
public class TransactionController {

    private final TransactionService transactionService;

    @Autowired
    public TransactionController(TransactionService transactionService) {
        this.transactionService = transactionService;
    }

    @PostMapping("/create")
    public ResponseEntity<MessageResponse> createTransaction(@RequestBody CreateTransactionRequest request,HttpServletRequest httpServletRequest) {
        MessageResponse response = transactionService.createTransaction(request, httpServletRequest);
        return new ResponseEntity<>(response, HttpStatus.CREATED);
    }

    @GetMapping("/get-by-id/{id}")
    public ResponseEntity<ApiDataResponseBuilder> getTransactionById(@PathVariable Long id) {
        ApiDataResponseBuilder response = transactionService.getTransactionById(id);
        return ResponseEntity.ok(response);
    }

    @GetMapping("/get-all")
    public ResponseEntity<ListResponse<TransactionDto>> getAllTransactions(
            Pageable pageable,
            @RequestParam(required = false) String productName,
            @RequestParam(required = false) String status,
            @RequestParam(required = false) Integer quantity) {
        ListResponse<TransactionDto> response = transactionService.getAllTransactions(pageable, productName, status, quantity);
        return ResponseEntity.ok(response);
    }
}

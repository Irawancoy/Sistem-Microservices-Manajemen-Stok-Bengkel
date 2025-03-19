package com.microservices.smmsb_transaction_service.controller;

import com.microservices.smmsb_transaction_service.dto.TransactionDto;
import com.microservices.smmsb_transaction_service.dto.request.CreateTransactionRequest;
import com.microservices.smmsb_transaction_service.dto.response.ApiDataResponseBuilder;
import com.microservices.smmsb_transaction_service.dto.response.ListResponse;
import com.microservices.smmsb_transaction_service.dto.response.MessageResponse;
import com.microservices.smmsb_transaction_service.service.TransactionService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1/transactions")
@Tag(name = "Transaction", description = "Endpoints for managing transactions")
public class TransactionController {

    private final TransactionService transactionService;

    @Autowired
    public TransactionController(TransactionService transactionService) {
        this.transactionService = transactionService;
    }

    @PostMapping("/create")
    @Operation(summary = "Create a new transaction", security = {
            @SecurityRequirement(name = "Bearer Authentication"),
            @SecurityRequirement(name = "X-Session-Id")
    })
    @ApiResponses(value = {
            @ApiResponse(responseCode = "201", description = "Transaction created successfully", content = @Content(schema = @Schema(implementation = MessageResponse.class))),
            @ApiResponse(responseCode = "400", description = "Invalid request"),
            @ApiResponse(responseCode = "401", description = "Unauthorized")
    })
    public ResponseEntity<MessageResponse> createTransaction(@RequestBody CreateTransactionRequest request,
            HttpServletRequest httpServletRequest) {
        MessageResponse response = transactionService.createTransaction(request, httpServletRequest);
        return new ResponseEntity<>(response, HttpStatus.CREATED);
    }

    @GetMapping("/get-by-id/{id}")
    @Operation(summary = "Get transaction by ID", security = {
            @SecurityRequirement(name = "Bearer Authentication"),
            @SecurityRequirement(name = "X-Session-Id")
    })
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Transaction found", content = @Content(schema = @Schema(implementation = ApiDataResponseBuilder.class))),
            @ApiResponse(responseCode = "404", description = "Transaction not found"),
            @ApiResponse(responseCode = "401", description = "Unauthorized")
    })
    public ResponseEntity<ApiDataResponseBuilder> getTransactionById(@PathVariable Long id) {
        ApiDataResponseBuilder response = transactionService.getTransactionById(id);
        return ResponseEntity.ok(response);
    }

    @GetMapping("/get-all")
    @Operation(summary = "Get all transactions", security = {
            @SecurityRequirement(name = "Bearer Authentication"),
            @SecurityRequirement(name = "X-Session-Id")
    })
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "List of transactions", content = @Content(schema = @Schema(implementation = ListResponse.class))),
            @ApiResponse(responseCode = "401", description = "Unauthorized")
    })
    public ResponseEntity<ListResponse<TransactionDto>> getAllTransactions(
            Pageable pageable,
            @Parameter(description = "Filter by product name") @RequestParam(required = false) String productName,
            @Parameter(description = "Filter by status") @RequestParam(required = false) String status,
            @Parameter(description = "Filter by quantity") @RequestParam(required = false) Integer quantity) {
        ListResponse<TransactionDto> response = transactionService.getAllTransactions(pageable, productName, status,
                quantity);
        return ResponseEntity.ok(response);
    }
}
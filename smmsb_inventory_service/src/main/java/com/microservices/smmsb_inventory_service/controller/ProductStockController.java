package com.microservices.smmsb_inventory_service.controller;

import com.microservices.smmsb_inventory_service.dto.ProductStockDto;
import com.microservices.smmsb_inventory_service.dto.request.CreateProductStockRequest;
import com.microservices.smmsb_inventory_service.dto.request.UpdateProductStockRequest;
import com.microservices.smmsb_inventory_service.dto.response.ApiDataResponseBuilder;
import com.microservices.smmsb_inventory_service.dto.response.ListResponse;
import com.microservices.smmsb_inventory_service.dto.response.MessageResponse;
import com.microservices.smmsb_inventory_service.service.ProductStockService;

import jakarta.servlet.http.HttpServletRequest;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;

import java.math.BigDecimal;
import java.util.Map;

@RestController
@RequestMapping("/api/v1/inventory")
@Tag(name = "Product Stock", description = "API operations related to product stock management")
public class ProductStockController {

    private final ProductStockService productStockService;

    @Autowired
    public ProductStockController(ProductStockService productStockService) {
        this.productStockService = productStockService;
    }

    // Create product stock
    @PostMapping("/create")
    @Operation(summary = "Create a new product stock", description = "Creates a new product stock entry.", tags = { "Product Stock" })
    @ApiResponses(value = {
            @ApiResponse(responseCode = "201", description = "Product stock created successfully", content = @Content(schema = @Schema(implementation = MessageResponse.class))),
            @ApiResponse(responseCode = "400", description = "Bad request (validation error)", content = @Content(schema = @Schema(implementation = MessageResponse.class))),
            @ApiResponse(responseCode = "409", description = "Product stock already exists", content = @Content(schema = @Schema(implementation = MessageResponse.class))),
            @ApiResponse(responseCode = "403", description = "Forbidden", content = @Content(schema = @Schema(implementation = MessageResponse.class)))
    })
    public MessageResponse createProductStock(@RequestBody CreateProductStockRequest createProductStockRequest,
            HttpServletRequest request) {
        return productStockService.createProductStock(createProductStockRequest, request);
    }

    // Update product stock
    @PutMapping("/update/{id}")
    @Operation(summary = "Update a product stock", description = "Updates an existing product stock information.", tags = { "Product Stock" })
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Product stock updated successfully", content = @Content(schema = @Schema(implementation = MessageResponse.class))),
            @ApiResponse(responseCode = "400", description = "Bad request (validation error)", content = @Content(schema = @Schema(implementation = MessageResponse.class))),
            @ApiResponse(responseCode = "404", description = "Product stock not found", content = @Content),
            @ApiResponse(responseCode = "403", description = "Forbidden", content = @Content(schema = @Schema(implementation = MessageResponse.class)))
    })
    public MessageResponse updateProductStock(@PathVariable Long id,
            @RequestBody UpdateProductStockRequest updateProductStockRequest, HttpServletRequest request) {
        return productStockService.updateProductStock(id, updateProductStockRequest, request);
    }

    // delete product stock
    @DeleteMapping("/delete/{id}")
    @Operation(summary = "Delete a product stock", description = "Deletes a product stock by its ID.", tags = { "Product Stock" })
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Product stock deleted successfully", content = @Content(schema = @Schema(implementation = MessageResponse.class))),
            @ApiResponse(responseCode = "404", description = "Product stock not found", content = @Content),
            @ApiResponse(responseCode = "403", description = "Forbidden", content = @Content(schema = @Schema(implementation = MessageResponse.class)))
    })
    public MessageResponse deleteProductStock(@PathVariable Long id, HttpServletRequest request) {
        return productStockService.deleteProductStock(id, request);
    }

    // get all product stock
    @GetMapping("/get-all")
    @Operation(summary = "Get all product stocks", description = "Retrieves a list of all product stocks with optional filters.", tags = { "Product Stock" })
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Successfully retrieved product stocks", content = @Content(schema = @Schema(implementation = ListResponse.class))),
            @ApiResponse(responseCode = "403", description = "Forbidden", content = @Content)
    })
    public ResponseEntity<ListResponse<ProductStockDto>> getAllProductStock(
            @PageableDefault(size = 10) Pageable pageable,
            @RequestParam(required = false) String productName,
            @RequestParam(required = false) BigDecimal price) {
        return ResponseEntity.ok(productStockService.getAllProductStocks(pageable, productName, price));
    }

    // get product stock by id
    @GetMapping("/get-by-id/{id}")
    @Operation(summary = "Get product stock by ID", description = "Retrieves a product stock by its ID.", tags = { "Product Stock" })
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Successfully retrieved product stock", content = @Content(schema = @Schema(implementation = ProductStockDto.class))),
            @ApiResponse(responseCode = "404", description = "Product stock not found", content = @Content),
            @ApiResponse(responseCode = "403", description = "Forbidden", content = @Content(schema = @Schema(implementation = MessageResponse.class)))
    })
    public ResponseEntity<ApiDataResponseBuilder> getProductStockById(@PathVariable Long id) {
        return ResponseEntity.ok(productStockService.getProductStockById(id));
    }

    // upload photo
    @PostMapping(value = "/upload-photo/{id}", consumes = MediaType.MULTIPART_FORM_DATA_VALUE, produces = MediaType.APPLICATION_JSON_VALUE)
    @Operation(summary = "Upload product stock photo", description = "Uploads a photo for a specific product stock.", tags = { "Product Stock" })
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Photo uploaded successfully", content = @Content(schema = @Schema(implementation = ListResponse.class))),
            @ApiResponse(responseCode = "400", description = "Bad request (invalid file)", content = @Content(schema = @Schema(implementation = MessageResponse.class))),
            @ApiResponse(responseCode = "404", description = "Product stock not found", content = @Content),
            @ApiResponse(responseCode = "403", description = "Forbidden", content = @Content(schema = @Schema(implementation = MessageResponse.class)))
    })
    public ResponseEntity<ListResponse<Map<String, String>>> uploadUserPhoto(
            @PathVariable Long id,
            @RequestParam("file") MultipartFile file) {
        ListResponse<Map<String, String>> response = productStockService.uploadImage(file, id);

        return ResponseEntity.ok(response);
    }
}

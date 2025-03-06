package com.microservices.smmsb_inventory_service.controller;


import com.microservices.smmsb_inventory_service.dto.ProductStockDto;
import com.microservices.smmsb_inventory_service.dto.request.CreateProductStockRequest;
import com.microservices.smmsb_inventory_service.dto.request.UpdateProductStockRequest;
import com.microservices.smmsb_inventory_service.dto.response.ApiDataResponseBuilder;
import com.microservices.smmsb_inventory_service.dto.response.ListResponse;
import com.microservices.smmsb_inventory_service.dto.response.MessageResponse;
import com.microservices.smmsb_inventory_service.service.ProductStockService;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.hateoas.EntityModel;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.math.BigDecimal;
import java.util.Map;

@RestController
@RequestMapping("/api/v1/product-stock")
public class ProductStockController {

    private final ProductStockService productStockService;

    @Autowired
    public ProductStockController(ProductStockService productStockService) {
        this.productStockService = productStockService;
    }

    // Create product stock
    @PostMapping("/create")
    public MessageResponse createProductStock(@RequestBody CreateProductStockRequest createProductStockRequest) {
        return productStockService.createProductStock(createProductStockRequest);
    }

    // Update product stock
    @PutMapping("/update/{id}")
    public MessageResponse updateProductStock(@PathVariable Long id,
            @RequestBody UpdateProductStockRequest updateProductStockRequest) {
        return productStockService.updateProductStock(id, updateProductStockRequest);
    }

    // delete product stock
    @DeleteMapping("/delete/{id}")
    public MessageResponse deleteProductStock(@PathVariable Long id) {
        return productStockService.deleteProductStock(id);
    }

    // get all product stock
    @GetMapping("/get-all")
    public ResponseEntity<ListResponse<EntityModel<ProductStockDto>>> getAllProductStock(
            @PageableDefault(size = 10) Pageable pageable,
            @RequestParam(required = false) String productName,
            @RequestParam(required = false) String changeType,
            @RequestParam(required = false) BigDecimal price) {
        return ResponseEntity.ok(productStockService.getAllProductStocks(pageable, productName, changeType, price));
    }

    // get product stock by id
    @GetMapping("/get-by-id/{id}")
    public ResponseEntity<ApiDataResponseBuilder> getProductStockById(@PathVariable Long id) {
        return ResponseEntity.ok(productStockService.getProductStockById(id));
    }

    // upload photo
    @PostMapping(value="/upload-photo/{id}", consumes = MediaType.MULTIPART_FORM_DATA_VALUE, produces = MediaType.APPLICATION_JSON_VALUE)
        public ResponseEntity<ListResponse<Map<String, String>>> uploadUserPhoto(
                @PathVariable Long id,
                @RequestParam("file") MultipartFile file){
            ListResponse<Map<String, String>> response = productStockService.uploadImage(file, id);
                    
            return ResponseEntity.ok(response);
                }
        

}

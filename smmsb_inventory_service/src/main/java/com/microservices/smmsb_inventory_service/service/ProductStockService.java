package com.microservices.smmsb_inventory_service.service;

import com.microservices.smmsb_inventory_service.dto.response.MessageResponse;
import com.microservices.smmsb_inventory_service.dto.request.CreateProductStockRequest;
import com.microservices.smmsb_inventory_service.dto.request.UpdateProductStockRequest;

import java.math.BigDecimal;
import java.util.Map;

import org.springframework.data.domain.Pageable;
import org.springframework.hateoas.EntityModel;
import org.springframework.web.multipart.MultipartFile;

import com.microservices.smmsb_inventory_service.dto.ProductStockDto;
import com.microservices.smmsb_inventory_service.dto.response.ApiDataResponseBuilder;
import com.microservices.smmsb_inventory_service.dto.response.ListResponse;

public interface ProductStockService {

   MessageResponse createProductStock(CreateProductStockRequest createProductStockRequest);

   MessageResponse updateProductStock(Long id, UpdateProductStockRequest updateProductStockRequest);

   ApiDataResponseBuilder getProductStockById(Long id);

   MessageResponse deleteProductStock(Long id);

   ListResponse<EntityModel<ProductStockDto>> getAllProductStocks(Pageable pageable, String productName,
         String changeType, BigDecimal price);

   ListResponse<Map<String, String>> uploadImage(MultipartFile file, Long id);

}
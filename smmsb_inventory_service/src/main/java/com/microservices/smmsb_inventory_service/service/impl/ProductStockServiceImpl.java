package com.microservices.smmsb_inventory_service.service.impl;

import com.microservices.smmsb_inventory_service.controller.ProductStockController;
import com.microservices.smmsb_inventory_service.dto.ProductStockDto;
import com.microservices.smmsb_inventory_service.dto.request.CreateProductStockRequest;
import com.microservices.smmsb_inventory_service.dto.request.UpdateProductStockRequest;
import com.microservices.smmsb_inventory_service.dto.response.ApiDataResponseBuilder;
import com.microservices.smmsb_inventory_service.dto.response.ListResponse;
import com.microservices.smmsb_inventory_service.dto.response.MessageResponse;
import com.microservices.smmsb_inventory_service.model.ProductStock;
import com.microservices.smmsb_inventory_service.repository.ProductStockRepository;
import com.microservices.smmsb_inventory_service.service.MinioService;
import com.microservices.smmsb_inventory_service.service.ProductStockService;
import com.microservices.smmsb_inventory_service.utils.MessageUtils;
import com.microservices.smmsb_inventory_service.utils.ProductStockSpesification;

import org.apache.kafka.common.errors.ResourceNotFoundException;
import org.modelmapper.ModelMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.hateoas.EntityModel;
import org.springframework.hateoas.Link;
import org.springframework.hateoas.server.mvc.WebMvcLinkBuilder;
import org.springframework.http.HttpStatus;
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

@Service
public class ProductStockServiceImpl implements ProductStockService {

    private final ProductStockRepository productStockRepository;
    private final ModelMapper modelMapper;
    private final MessageUtils messageUtils;
    private final MinioService minioService;

    @Autowired
    public ProductStockServiceImpl(ProductStockRepository productStockRepository, ModelMapper modelMapper,
            MessageUtils messageUtils, MinioService minioService) {
        this.productStockRepository = productStockRepository;
        this.modelMapper = modelMapper;
        this.messageUtils = messageUtils;
        this.minioService = minioService;
    }

    @Override
    @Transactional
    public MessageResponse createProductStock(CreateProductStockRequest createProductStockRequest) {
        ProductStock productStock = modelMapper.map(createProductStockRequest, ProductStock.class);
        productStockRepository.save(productStock);
        return new MessageResponse(
                messageUtils.getMessage("product.created", productStock.getProductName()),
                HttpStatus.CREATED.value(),
                HttpStatus.CREATED.name());
    }

    @Override
    @Transactional
    public MessageResponse updateProductStock(Long id, UpdateProductStockRequest updateProductStockRequest) {
        ProductStock productStock = productStockRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Product not found with id: " + id));
        modelMapper.map(updateProductStockRequest, productStock);
        productStockRepository.save(productStock);
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
    public MessageResponse deleteProductStock(Long id) {
        ProductStock productStock = productStockRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Product not found with id: " + id));
        productStockRepository.delete(productStock);
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

    String objectName = "products/" + Id + "_" + System.currentTimeMillis() + "_" + file.getOriginalFilename();
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
    public ListResponse<EntityModel<ProductStockDto>> getAllProductStocks(Pageable pageable, String productName,
            String changeType, BigDecimal price) {
        Specification<ProductStock> spec = Specification.where(null);
        if (productName != null) {
            spec = spec.and(ProductStockSpesification.hasProductName(productName));
        }
        if (changeType != null) {
            spec = spec.and(ProductStockSpesification.hasChangeType(changeType));
        }
        if (price != null) {
            spec = spec.and(ProductStockSpesification.hasPrice(price));
        }

        Page<ProductStock> productStocks = productStockRepository.findAll(spec, pageable);
        List<EntityModel<ProductStockDto>> productStockDtos = productStocks.getContent().stream().map(product -> {
            ProductStockDto productStockDto = modelMapper.map(product, ProductStockDto.class);
            Link selfLink = WebMvcLinkBuilder.linkTo(WebMvcLinkBuilder.methodOn(ProductStockController.class)
                    .getProductStockById(product.getId())).withSelfRel();
            Link updateLink = WebMvcLinkBuilder.linkTo(WebMvcLinkBuilder.methodOn(ProductStockController.class)
                    .updateProductStock(product.getId(), null)).withRel("update");
            Link deleteLink = WebMvcLinkBuilder.linkTo(WebMvcLinkBuilder.methodOn(ProductStockController.class)
                    .deleteProductStock(product.getId())).withRel("delete");
            return EntityModel.of(productStockDto, selfLink, updateLink, deleteLink);
        }).collect(Collectors.toList());

        return new ListResponse<>(productStockDtos, messageUtils.getMessage("products.found"), HttpStatus.OK.value(),
                HttpStatus.OK.name());
    }

    
    private String getFileNameFromUrl(String url) {
        return url.substring(url.lastIndexOf("/") + 1);
    }

        
}
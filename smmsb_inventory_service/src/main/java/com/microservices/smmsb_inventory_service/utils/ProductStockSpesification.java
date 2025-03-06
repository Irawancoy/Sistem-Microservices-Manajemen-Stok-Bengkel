package com.microservices.smmsb_inventory_service.utils;

import java.math.BigDecimal;

import org.springframework.data.jpa.domain.Specification;
import com.microservices.smmsb_inventory_service.model.ProductStock;

public class ProductStockSpesification {

   public static Specification<ProductStock> hasProductName(String productName) {
      return (root, query, criteriaBuilder) -> criteriaBuilder.like(root.get("productName"), "%" + productName + "%");
   }
    
   public static Specification<ProductStock> hasChangeType(String changeType) {
      return (root, query, criteriaBuilder) -> criteriaBuilder.like(root.get("changeType"), "%" + changeType + "%");
   }

   public static Specification<ProductStock> hasPrice(BigDecimal price) {
      return (root, query, criteriaBuilder) -> criteriaBuilder.equal(root.get("price"), price);
   }
   
}

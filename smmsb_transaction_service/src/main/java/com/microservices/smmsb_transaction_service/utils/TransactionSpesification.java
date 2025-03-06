package com.microservices.smmsb_transaction_service.utils;

import org.springframework.data.jpa.domain.Specification;
import com.microservices.smmsb_transaction_service.model.Transaction;

public class TransactionSpesification {

   public static Specification<Transaction> filterByQuantity(Integer quantity) {
      return (root, query, criteriaBuilder) -> criteriaBuilder.equal(root.get("quantity"), quantity);
   }

   public static Specification<Transaction> filterByStatus(String status) {
      return (root, query, criteriaBuilder) -> criteriaBuilder.equal(root.get("status"), status);
   }

   public static Specification<Transaction> filterProductName(String productName) {
      return (root, query, criteriaBuilder) -> criteriaBuilder.like(root.get("productName"), "%" + productName + "%");
   }
}

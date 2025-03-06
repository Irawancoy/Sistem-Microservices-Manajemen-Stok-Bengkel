package com.microservices.smmsb_inventory_service.repository;

import com.microservices.smmsb_inventory_service.model.ProductStock;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.stereotype.Repository;

@Repository
public interface ProductStockRepository extends JpaRepository<ProductStock, Long>,JpaSpecificationExecutor<ProductStock> {
    // Custom query to find products with low stock
    List<ProductStock> findByQuantityLessThan(int threshold);
}

package com.microservices.smmsb_transaction_service.model;

import jakarta.persistence.*;
import lombok.*;

import java.math.BigDecimal;
import java.time.LocalDateTime;

import org.hibernate.annotations.CreationTimestamp;

@Entity
@Table(name = "transactions")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Transaction {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private Long userId;

    @Column(nullable = false)
    private Long productId; // ID produk dari Product Service

    @Column(nullable = false)
    private String productName; // Nama produk dari Product Service

    @Column(nullable = false)
    private BigDecimal price; // Harga produk saat transaksi

    @Column(nullable = false)
    private Integer quantity;

    @Column(nullable = false)
    private BigDecimal totalAmount;
    
    @CreationTimestamp
    private LocalDateTime createdAt;
}
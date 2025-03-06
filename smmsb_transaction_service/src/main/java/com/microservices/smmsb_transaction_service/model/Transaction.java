package com.microservices.smmsb_transaction_service.model;

import jakarta.persistence.*;
import lombok.*;

import java.math.BigDecimal;
import java.time.LocalDateTime;

import org.hibernate.annotations.CreationTimestamp;

@Entity
@Table(name = "transactions")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Transaction {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private Long userId;
    private Long productId; // ID produk dari Product Service
    private String productName; // Nama produk dari Product Service
    private BigDecimal price; // Harga produk saat transaksi
    private Integer quantity;
    private BigDecimal totalAmount;
    @CreationTimestamp
    private LocalDateTime createdAt;
}
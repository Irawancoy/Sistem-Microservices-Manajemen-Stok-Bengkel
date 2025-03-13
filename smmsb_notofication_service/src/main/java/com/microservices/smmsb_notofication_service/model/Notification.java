package com.microservices.smmsb_notofication_service.model;

import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;

import org.hibernate.annotations.CreationTimestamp;

@Entity
@Table(name = "notifications")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Notification {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "user_id", nullable =true)
    private Long userId;

    @Column(nullable = false, columnDefinition = "TEXT")
    private String message;

    private String type;

    @CreationTimestamp
    private LocalDateTime createdAt;
}

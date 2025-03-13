package com.microservices.smmsb_notofication_service.repository;

import com.microservices.smmsb_notofication_service.model.Notification;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;

public interface NotificationRepository extends JpaRepository<Notification, Long> ,JpaSpecificationExecutor<Notification> {
}


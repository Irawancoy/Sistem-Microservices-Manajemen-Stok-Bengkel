package com.microservices.smmsb_notofication_service.utils;

import org.springframework.data.jpa.domain.Specification;
import com.microservices.smmsb_notofication_service.model.Notification;

public class NotificationSpecification {

   public static Specification<Notification> hasUserId(Long userId) {
      return (root, query, criteriaBuilder) -> criteriaBuilder.equal(root.get("userId"), userId);
   }
    
   public static Specification<Notification> hasType(String type) {
      return (root, query, criteriaBuilder) -> criteriaBuilder.equal(root.get("type"), type);
   }
   public static Specification<Notification> hasMessage(String message) {
      return (root, query, criteriaBuilder) -> criteriaBuilder.like(root.get("message"), "%" + message + "%");
   }

   
   
}

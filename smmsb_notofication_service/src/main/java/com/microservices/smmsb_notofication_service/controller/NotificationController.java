package com.microservices.smmsb_notofication_service.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.microservices.smmsb_notofication_service.dto.NotificationDto;
import com.microservices.smmsb_notofication_service.dto.response.ApiDataResponseBuilder;
import com.microservices.smmsb_notofication_service.dto.response.ListResponse;
import com.microservices.smmsb_notofication_service.service.NotificationService;

@RestController
@RequestMapping("/api/v1/notifications")
public class NotificationController {

   private final NotificationService notificationService;

   @Autowired
   public NotificationController(NotificationService notificationService) {
      this.notificationService = notificationService;
   }

   // Get notification by ID
   @GetMapping("/get-by-id/{id}")
   public ResponseEntity<ApiDataResponseBuilder> getNotificationById(@PathVariable Long id) {
      ApiDataResponseBuilder response = notificationService.getNotificationById(id);
      return ResponseEntity.ok(response);
   }

   // Get all notifications
   @GetMapping("/get-all")
   public ResponseEntity<ListResponse<NotificationDto>> getAllNotifications(
         @PageableDefault(size = 10) Pageable pageable,
         @RequestParam(required = false) String userId,
         @RequestParam(required = false) String type,
         @RequestParam(required = false) String message) {
      ListResponse<NotificationDto> response = notificationService.getAllNotifications(pageable, userId, type, message);
      return ResponseEntity.ok(response);
   }

}

package com.microservices.smmsb_notofication_service.service.impl;

import java.util.List;
import java.util.stream.Collectors;

import org.modelmapper.ModelMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;

import com.microservices.smmsb_notofication_service.dto.NotificationDto;
import com.microservices.smmsb_notofication_service.dto.response.ApiDataResponseBuilder;
import com.microservices.smmsb_notofication_service.dto.response.ListResponse;
import com.microservices.smmsb_notofication_service.model.Notification;
import com.microservices.smmsb_notofication_service.repository.NotificationRepository;
import com.microservices.smmsb_notofication_service.service.NotificationService;
import com.microservices.smmsb_notofication_service.utils.MessageUtils;
import com.microservices.smmsb_notofication_service.utils.NotificationSpecification;

@Service
public class NotificationServiceImpl implements NotificationService {

   private final NotificationRepository notificationRepository;
   private final ModelMapper modelMapper;
   private final MessageUtils messageUtils;

   @Autowired
   public NotificationServiceImpl(NotificationRepository notificationRepository, ModelMapper modelMapper,
         MessageUtils messageUtils) {
      this.notificationRepository = notificationRepository;
      this.modelMapper = modelMapper;
      this.messageUtils = messageUtils;
   }

   @Override
   public ApiDataResponseBuilder getNotificationById(Long id) {
      Notification notification = notificationRepository.findById(id)
              .orElseThrow(() -> new RuntimeException(messageUtils.getMessage("notification.not.found")));
      NotificationDto notificationDto = modelMapper.map(notification, NotificationDto.class);
      return ApiDataResponseBuilder.builder()
              .data(notificationDto)
              .message(messageUtils.getMessage("notification.retrive.success"))
              .status(HttpStatus.OK)
              .statusCode(HttpStatus.OK.value())
              .build();
   }

   @Override
   public ListResponse<NotificationDto> getAllNotifications(Pageable pageable, Long userId, String type,
         String message) {

      Specification<Notification> spec = Specification.where(null);
      if (userId != null) {
         spec = spec.and(NotificationSpecification.hasUserId(userId));
      }
      if (type != null) {
         spec = spec.and(NotificationSpecification.hasType(type));
      }
      if (message != null) {
         spec = spec.and(NotificationSpecification.hasMessage(message));
      }

         Page<Notification> notifications = notificationRepository.findAll(spec, pageable);

         List<NotificationDto> notificationDtos = notifications.getContent().stream().map(
               notification -> modelMapper.map(notification, NotificationDto.class)).collect(Collectors.toList());

         return new ListResponse<>(
               notificationDtos, messageUtils.getMessage("notification.retrive.success"), HttpStatus.OK.value(),
               HttpStatus.OK.name());
      
   }
      
         
            
    
   

}

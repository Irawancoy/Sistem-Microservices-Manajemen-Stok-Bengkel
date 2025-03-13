package com.microservices.smmsb_notofication_service.service;

import com.microservices.smmsb_notofication_service.dto.response.ApiDataResponseBuilder;
import com.microservices.smmsb_notofication_service.dto.response.ListResponse;

import org.springframework.data.domain.Pageable;

import com.microservices.smmsb_notofication_service.dto.NotificationDto;   

public interface NotificationService {

   ApiDataResponseBuilder getNotificationById(Long id);

   ListResponse<NotificationDto> getAllNotifications(Pageable pageable,String userId,String type,String message);
}

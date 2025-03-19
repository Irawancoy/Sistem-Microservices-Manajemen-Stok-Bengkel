package com.microservices.smmsb_notofication_service.service.impl;

import com.microservices.smmsb_notofication_service.dto.NotificationDto;
import com.microservices.smmsb_notofication_service.dto.response.ApiDataResponseBuilder;
import com.microservices.smmsb_notofication_service.dto.response.ListResponse;
import com.microservices.smmsb_notofication_service.model.Notification;
import com.microservices.smmsb_notofication_service.repository.NotificationRepository;
import com.microservices.smmsb_notofication_service.utils.MessageUtils;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.modelmapper.ModelMapper;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.http.HttpStatus;

import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class NotificationServiceImplTest {

    @Mock
    private NotificationRepository notificationRepository;

    @Mock
    private ModelMapper modelMapper;

    @Mock
    private MessageUtils messageUtils;

    @InjectMocks
    private NotificationServiceImpl notificationService;

    private Notification notification;
    private NotificationDto notificationDto;
    private final Long notificationId = 1L;
    private final Long userId = 123L;
    private final String type = "INFO";
    private final String message = "Test notification";
    private final String successMessage = "Notification retrieved successfully";
    private final String notFoundMessage = "Notification not found";

    @BeforeEach
    void setUp() {
        // Setup test data
        notification = new Notification();
        notification.setId(notificationId);
        notification.setUserId(userId);
        notification.setType(type);
        notification.setMessage(message);
        notification.setCreatedAt(LocalDateTime.now());

        notificationDto = new NotificationDto();
        notificationDto.setId(notificationId);
        notificationDto.setUserId(userId);
        notificationDto.setType(type);
        notificationDto.setMessage(message);
        
        // Removed the message utils stubbing from here
    }

    @Test
    void getNotificationById_WhenNotificationExists_ShouldReturnNotification() {
        // Given
        when(messageUtils.getMessage("notification.retrive.success")).thenReturn(successMessage);

        when(notificationRepository.findById(notificationId)).thenReturn(Optional.of(notification));
        when(modelMapper.map(notification, NotificationDto.class)).thenReturn(notificationDto);

        // When
        ApiDataResponseBuilder response = notificationService.getNotificationById(notificationId);

        // Then
        assertNotNull(response);
        assertEquals(HttpStatus.OK.value(), response.getStatusCode());
        assertEquals(successMessage, response.getMessage());
        assertEquals(notificationDto, response.getData());
        
        verify(notificationRepository).findById(notificationId);
        verify(modelMapper).map(notification, NotificationDto.class);
    }

    @Test
    void getNotificationById_WhenNotificationDoesNotExist_ShouldThrowException() {
        // Given
        when(messageUtils.getMessage("notification.not.found")).thenReturn(notFoundMessage);
        when(notificationRepository.findById(notificationId)).thenReturn(Optional.empty());

        // When & Then
        RuntimeException exception = assertThrows(RuntimeException.class, () -> {
            notificationService.getNotificationById(notificationId);
        });
        
        assertEquals(notFoundMessage, exception.getMessage());
        verify(notificationRepository).findById(notificationId);
        verify(modelMapper, never()).map(any(), any());
    }

    @SuppressWarnings("unchecked")
   @Test
    void getAllNotifications_WithNoFilters_ShouldReturnAllNotifications() {
        // Given
        when(messageUtils.getMessage("notification.retrive.success")).thenReturn(successMessage);
        Pageable pageable = PageRequest.of(0, 10);
        List<Notification> notifications = Arrays.asList(notification);
        Page<Notification> page = new PageImpl<>(notifications, pageable, notifications.size());
        
        when(notificationRepository.findAll(any(Specification.class), eq(pageable))).thenReturn(page);
        when(modelMapper.map(notification, NotificationDto.class)).thenReturn(notificationDto);

        // When
        ListResponse<NotificationDto> response = notificationService.getAllNotifications(pageable, null, null, null);

        // Then
        assertNotNull(response);
        assertEquals(HttpStatus.OK.name(), response.getStatus());
        assertEquals(successMessage, response.getMessage());
        assertEquals(1, response.getData().size());
        assertEquals(notificationDto, response.getData().get(0));
        
        verify(notificationRepository).findAll(any(Specification.class), eq(pageable));
        verify(modelMapper).map(notification, NotificationDto.class);
    }

    @SuppressWarnings({ "unchecked", "rawtypes" })
   @Test
    void getAllNotifications_WithFilters_ShouldApplyFilters() {
        // Given
        when(messageUtils.getMessage("notification.retrive.success")).thenReturn(successMessage);
        Pageable pageable = PageRequest.of(0, 10);
        List<Notification> notifications = Arrays.asList(notification);
        Page<Notification> page = new PageImpl<>(notifications, pageable, notifications.size());
        
        when(notificationRepository.findAll(any(Specification.class), eq(pageable))).thenReturn(page);
        when(modelMapper.map(notification, NotificationDto.class)).thenReturn(notificationDto);

        // When
        ListResponse<NotificationDto> response = notificationService.getAllNotifications(pageable, userId, type, message);

        // Then
        assertNotNull(response);
        assertEquals(HttpStatus.OK.name(), response.getStatus());
        assertEquals(successMessage, response.getMessage());
        assertEquals(1, response.getData().size());
        assertEquals(notificationDto, response.getData().get(0));
        
        // Verify that the repository was called with the correct specification
        ArgumentCaptor<Specification> specCaptor = ArgumentCaptor.forClass(Specification.class);
        verify(notificationRepository).findAll(specCaptor.capture(), eq(pageable));
        
        // We can't easily test the exact specification content, but we can verify it was called
        assertNotNull(specCaptor.getValue());
        verify(modelMapper).map(notification, NotificationDto.class);
    }

    @SuppressWarnings("unchecked")
   @Test
    void getAllNotifications_WithEmptyResult_ShouldReturnEmptyList() {
        // Given
        when(messageUtils.getMessage("notification.retrive.success")).thenReturn(successMessage);
        Pageable pageable = PageRequest.of(0, 10);
        Page<Notification> emptyPage = new PageImpl<>(List.of(), pageable, 0);
        
        when(notificationRepository.findAll(any(Specification.class), eq(pageable))).thenReturn(emptyPage);

        // When
        ListResponse<NotificationDto> response = notificationService.getAllNotifications(pageable, userId, type, message);

        // Then
        assertNotNull(response);
        assertEquals(HttpStatus.OK.name(), response.getStatus());
        assertEquals(successMessage, response.getMessage());
        assertTrue(response.getData().isEmpty());
        
        verify(notificationRepository).findAll(any(Specification.class), eq(pageable));
        verify(modelMapper, never()).map(any(), any()); // ModelMapper should not be called for empty list
    }
}

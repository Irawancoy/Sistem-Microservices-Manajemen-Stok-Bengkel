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

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;

@RestController
@RequestMapping("/api/v1/notifications")
@Tag(name = "Notifications", description = "API operations related to notifications management")
public class NotificationController {

    private final NotificationService notificationService;

    @Autowired
    public NotificationController(NotificationService notificationService) {
        this.notificationService = notificationService;
    }

    // Get notification by ID
    @GetMapping("/get-by-id/{id}")
    @Operation(summary = "Get notification by ID", description = "Retrieves a notification by its ID.", tags = { "Notifications" })
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Successfully retrieved notification", content = @Content(schema = @Schema(implementation = ApiDataResponseBuilder.class))),
            @ApiResponse(responseCode = "404", description = "Notification not found", content = @Content),
            @ApiResponse(responseCode = "403", description = "Forbidden", content = @Content(schema = @Schema(implementation = ApiDataResponseBuilder.class)))
    })
    public ResponseEntity<ApiDataResponseBuilder> getNotificationById(@PathVariable Long id) {
        ApiDataResponseBuilder response = notificationService.getNotificationById(id);
        return ResponseEntity.ok(response);
    }

    // Get all notifications
    @GetMapping("/get-all")
    @Operation(summary = "Get all notifications", description = "Retrieves a list of all notifications with optional filters.", tags = { "Notifications" })
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Successfully retrieved notifications", content = @Content(schema = @Schema(implementation = ListResponse.class))),
            @ApiResponse(responseCode = "403", description = "Forbidden", content = @Content)
    })
    public ResponseEntity<ListResponse<NotificationDto>> getAllNotifications(
            @PageableDefault(size = 10) Pageable pageable,
            @RequestParam(required = false) String userId,
            @RequestParam(required = false) String type,
            @RequestParam(required = false) String message) {
        ListResponse<NotificationDto> response = notificationService.getAllNotifications(pageable, userId, type, message);
        return ResponseEntity.ok(response);
    }
}

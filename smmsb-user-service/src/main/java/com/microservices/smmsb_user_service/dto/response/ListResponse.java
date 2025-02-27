package com.microservices.smmsb_user_service.dto.response;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class ListResponse<T> {
    private List<T> data;
    private String message;
    private int statusCode;
    private String status;
}

package com.microservices.smmsb_inventory_service.dto.response;
import lombok.Builder;
import lombok.Getter;
import org.springframework.http.HttpStatus;

@Getter
public class ApiDataResponseBuilder {
    private Object data;
    private String message;
    private int statusCode;
    private HttpStatus status;
    
    @Builder
    public ApiDataResponseBuilder(Object data, String message, int statusCode, HttpStatus status) {
        this.data = data;
        this.message = message;
        this.statusCode = statusCode;
        this.status = status;
    }
}

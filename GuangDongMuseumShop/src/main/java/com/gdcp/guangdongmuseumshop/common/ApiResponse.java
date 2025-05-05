package com.gdcp.guangdongmuseumshop.common;

import lombok.Data;

@Data
public class ApiResponse<T> {
    private int code;
    private String message;
    private T data;

    public static <T> ApiResponse<T> success(T data) {
        ApiResponse<T> response = new ApiResponse<>();
        response.setCode(200);
        response.setMessage("success");
        response.setData(data);
        return response;
    }

    public static ApiResponse<String> error(String message) {
        ApiResponse<String> response = new ApiResponse<>();
        response.setCode(500);
        response.setMessage(message);
        return response;
    }
} 
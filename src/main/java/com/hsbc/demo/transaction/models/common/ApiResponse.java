package com.hsbc.demo.transaction.models.common;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import static com.hsbc.demo.transaction.models.common.ApiErrorCode.SUCCESS;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class ApiResponse<T> {
    private Integer code;
    private String message;
    private T data;

    public static <T> ApiResponse<T> fail(ApiErrorCode errorCode) {
        return new ApiResponse<>(errorCode.getCode(), errorCode.getMessage(), null);
    }

    public static <T> ApiResponse<T> fail(ApiException apiException) {
        return new ApiResponse<>(apiException.getCode(), apiException.getMessage(), null);
    }

    public static <T> ApiResponse<T> fail(int errCode, String message) {
        return new ApiResponse<>(errCode, message, null);
    }

    public static <T> ApiResponse<T> success(T data) {
        return new ApiResponse<>(SUCCESS.getCode(), SUCCESS.getMessage(), data);
    }
}

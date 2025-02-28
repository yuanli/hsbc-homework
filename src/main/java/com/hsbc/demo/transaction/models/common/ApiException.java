package com.hsbc.demo.transaction.models.common;

import lombok.Data;
import lombok.EqualsAndHashCode;

@EqualsAndHashCode(callSuper = true)
@Data
public class ApiException extends RuntimeException {
    private final Integer code;
    private final String message;
    private final Throwable originError;

    public ApiException(ApiErrorCode code) {
        this.code = code.getCode();
        this.message = code.getMessage();
        originError = new Exception();
    }

    public ApiException(Integer code, String message, Throwable originError) {
        this.code = code;
        this.message = message;
        this.originError = originError;
    }

}

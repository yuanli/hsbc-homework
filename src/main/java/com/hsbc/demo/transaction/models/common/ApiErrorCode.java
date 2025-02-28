package com.hsbc.demo.transaction.models.common;

import lombok.Getter;

@Getter
public enum ApiErrorCode {

    SUCCESS(0, "success"),

    // 通用错误码
    ERR_COMMON(-1, "unknown error"),
    ERR_INVALID_PARAMS(-2, "invalid_params"),

    // 具体的业务错误码
    ERR_TRANSACTION_EXISTS(-1000, "transaction exists"),
    ERR_INVALID_TRANSACTION_ID(-1001, "invalid transaction id"),
    ERR_DATA_HAS_BEEN_MODIFIED(-1002, "data has been modified"),
    ;


    private final int code;
    private final String message;

    ApiErrorCode(int code, String message) {
        this.code = code;
        this.message = message;
    }

}

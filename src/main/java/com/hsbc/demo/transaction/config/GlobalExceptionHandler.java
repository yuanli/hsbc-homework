package com.hsbc.demo.transaction.config;

import com.hsbc.demo.transaction.models.common.ApiErrorCode;
import com.hsbc.demo.transaction.models.common.ApiException;
import com.hsbc.demo.transaction.models.common.ApiResponse;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.MessageSourceResolvable;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.HttpStatusCode;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.ObjectError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.context.request.WebRequest;
import org.springframework.web.method.annotation.HandlerMethodValidationException;
import org.springframework.web.servlet.mvc.method.annotation.ResponseEntityExceptionHandler;

@Slf4j
@ControllerAdvice
public class GlobalExceptionHandler extends ResponseEntityExceptionHandler {

    /**
     * 未知异常类型
     */
    @ExceptionHandler(Exception.class)
    public ResponseEntity<?> handleAllExceptions(Exception ex) {
        // 创建一个响应体，可以是自定义的错误信息对象
        ApiResponse<?> apiError = ApiResponse.fail(ApiErrorCode.ERR_COMMON);
        log.error("unhandled exception", ex);
        return new ResponseEntity<>(apiError, HttpStatus.INTERNAL_SERVER_ERROR);
    }


    @ExceptionHandler(ApiException.class)
    public ResponseEntity<?> handleApiErrorException(Exception ex) {
        ApiException apiException = (ApiException) ex;
        log.error("api error， errorCode = {}, msg = {}", apiException.getCode(), apiException.getMessage(), ex);
        return new ResponseEntity<>(ApiResponse.fail(apiException), HttpStatus.INTERNAL_SERVER_ERROR);
    }


    @Override
    protected ResponseEntity<Object> handleMethodArgumentNotValid(MethodArgumentNotValidException ex, HttpHeaders headers, HttpStatusCode status, WebRequest request) {
        ObjectError objectError = ex.getAllErrors().getFirst();
        return new ResponseEntity<>(ApiResponse.fail(ApiErrorCode.ERR_INVALID_PARAMS.getCode(), objectError.getDefaultMessage()), HttpStatus.INTERNAL_SERVER_ERROR);
    }

    @Override
    protected ResponseEntity<Object> handleHandlerMethodValidationException(HandlerMethodValidationException ex, HttpHeaders headers, HttpStatusCode status, WebRequest request) {
        MessageSourceResolvable objectError = ex.getAllErrors().getFirst();
        return new ResponseEntity<>(ApiResponse.fail(ApiErrorCode.ERR_INVALID_PARAMS.getCode(), objectError.getDefaultMessage()), HttpStatus.INTERNAL_SERVER_ERROR);
    }
}
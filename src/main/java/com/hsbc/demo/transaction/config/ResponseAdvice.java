package com.hsbc.demo.transaction.config;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.hsbc.demo.transaction.models.common.ApiErrorCode;
import com.hsbc.demo.transaction.models.common.ApiResponse;
import org.springframework.core.MethodParameter;
import org.springframework.http.CacheControl;
import org.springframework.http.MediaType;
import org.springframework.http.ProblemDetail;
import org.springframework.http.converter.HttpMessageConverter;
import org.springframework.http.server.ServerHttpRequest;
import org.springframework.http.server.ServerHttpResponse;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.servlet.mvc.method.annotation.ResponseBodyAdvice;

@RestControllerAdvice
public class ResponseAdvice implements ResponseBodyAdvice<Object> {
    private final ObjectMapper objectMapper;

    public ResponseAdvice(ObjectMapper objectMapper) {
        this.objectMapper = objectMapper;
    }

    @Override
    public boolean supports(MethodParameter methodParameter, Class<? extends HttpMessageConverter<?>> aClass) {
        return true;
    }

    @Override
    public Object beforeBodyWrite(Object body, MethodParameter returnType, MediaType selectedContentType,
                                  Class<? extends HttpMessageConverter<?>> selectedConverterType,
                                  ServerHttpRequest request, ServerHttpResponse response) {
        // 统一设置返回类型并禁用缓存
        response.getHeaders().setContentType(MediaType.APPLICATION_JSON);
        response.getHeaders().setCacheControl(CacheControl.noCache().getHeaderValue());
        response.getHeaders().setPragma("no-cache");
        response.getHeaders().setExpires(0);

        if (body instanceof String) {
            try {
                return objectMapper.writeValueAsString(ApiResponse.success(body));
            } catch (JsonProcessingException e) {
                throw new RuntimeException(e);
            }
        }

        if (body instanceof ProblemDetail) {
            return ApiResponse.fail(ApiErrorCode.ERR_COMMON.getCode(), ((ProblemDetail) body).getDetail());
        }

        if (body instanceof ApiResponse<?>) {
            return body;
        }
        return ApiResponse.success(body);

    }
}
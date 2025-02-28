package com.hsbc.demo.transaction.config;

import com.google.common.cache.Cache;
import com.google.common.cache.CacheBuilder;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.concurrent.TimeUnit;

@Configuration
public class GuavaCacheConfig {

    @Bean
    public Cache<Long, String> guavaCache() {
        return CacheBuilder.newBuilder()
                .maximumSize(1000 * 10) // 最大缓存条目数
                .expireAfterWrite(5, TimeUnit.MINUTES) // 缓存过期时间
                .build();
    }
}
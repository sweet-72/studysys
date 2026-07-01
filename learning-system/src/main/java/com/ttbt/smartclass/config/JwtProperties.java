package com.ttbt.smartclass.config;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

@Data
@Component
@ConfigurationProperties(prefix = "smartclass.jwt")
public class JwtProperties {

    private String secret = "smartclass-jwt-change-me-in-production";

    private long expireSeconds = 2592000L;

    private String header = "Authorization";

    private String tokenPrefix = "Bearer ";
}
package com.smartstore.smartstore.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

@Component
public class JwtConfig {
    @Value("${jwt.secret}")
    public String secret;

    @Value("${jwt.expiration}")
    public long expiration;
}

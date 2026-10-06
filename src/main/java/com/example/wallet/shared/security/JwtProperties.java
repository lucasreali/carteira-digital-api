package com.example.wallet.shared.security;

import java.time.Duration;
import org.springframework.boot.context.properties.ConfigurationProperties;

/** Binds {@code wallet.security.*}: the HMAC secret (at least 32 bytes) and the access token lifetime. */
@ConfigurationProperties("wallet.security")
public record JwtProperties(String jwtSecret, Duration tokenTtl) {}

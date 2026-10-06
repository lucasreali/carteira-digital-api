package com.example.wallet.wallets;

import java.time.Clock;
import java.time.Duration;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class WalletsModuleConfig {

    /** UTC, truncated to microseconds so in-memory instants equal what TIMESTAMP(6) columns store. */
    @Bean
    Clock clock() {
        return Clock.tick(Clock.systemUTC(), Duration.ofNanos(1_000));
    }
}

package com.example.wallet.wallets;

import java.time.Clock;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class WalletsModuleConfig {

    @Bean
    Clock clock() {
        return Clock.systemUTC();
    }
}

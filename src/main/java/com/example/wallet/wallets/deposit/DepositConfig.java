package com.example.wallet.wallets.deposit;

import com.example.wallet.wallets.domain.WalletRepository;
import java.time.Clock;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
class DepositConfig {

    @Bean
    DepositUseCase depositUseCase(WalletRepository wallets, Clock clock) {
        return new DepositInteractor(wallets, clock);
    }
}

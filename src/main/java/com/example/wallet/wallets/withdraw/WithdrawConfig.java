package com.example.wallet.wallets.withdraw;

import com.example.wallet.wallets.domain.WalletRepository;
import java.time.Clock;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
class WithdrawConfig {

    @Bean
    WithdrawUseCase withdrawUseCase(WalletRepository wallets, Clock clock) {
        return new WithdrawInteractor(wallets, clock);
    }
}

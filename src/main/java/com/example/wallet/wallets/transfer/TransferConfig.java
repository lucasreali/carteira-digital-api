package com.example.wallet.wallets.transfer;

import com.example.wallet.wallets.domain.WalletRepository;
import java.time.Clock;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
class TransferConfig {

    @Bean
    TransferUseCase transferUseCase(WalletRepository wallets, Clock clock) {
        return new TransferInteractor(wallets, clock);
    }
}

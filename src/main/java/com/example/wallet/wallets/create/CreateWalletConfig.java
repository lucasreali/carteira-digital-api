package com.example.wallet.wallets.create;

import com.example.wallet.wallets.domain.WalletRepository;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
class CreateWalletConfig {

    @Bean
    CreateWalletUseCase createWalletUseCase(WalletRepository wallets) {
        return new CreateWalletInteractor(wallets);
    }
}

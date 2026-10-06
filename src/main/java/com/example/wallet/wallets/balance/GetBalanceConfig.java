package com.example.wallet.wallets.balance;

import com.example.wallet.wallets.domain.WalletRepository;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
class GetBalanceConfig {

    @Bean
    GetBalanceUseCase getBalanceUseCase(WalletRepository wallets) {
        return new GetBalanceInteractor(wallets);
    }
}

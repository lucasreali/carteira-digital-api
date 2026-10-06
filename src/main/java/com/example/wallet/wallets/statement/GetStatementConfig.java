package com.example.wallet.wallets.statement;

import com.example.wallet.wallets.domain.TransactionHistory;
import com.example.wallet.wallets.domain.WalletRepository;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
class GetStatementConfig {

    @Bean
    GetStatementUseCase getStatementUseCase(WalletRepository wallets, TransactionHistory transactionHistory) {
        return new GetStatementInteractor(wallets, transactionHistory);
    }
}

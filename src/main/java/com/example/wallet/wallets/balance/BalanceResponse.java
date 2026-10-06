package com.example.wallet.wallets.balance;

import com.example.wallet.wallets.domain.Money;
import java.math.BigDecimal;
import java.util.UUID;

public record BalanceResponse(UUID walletId, BigDecimal balance) {

    static BalanceResponse from(UUID walletId, Money balance) {
        return new BalanceResponse(walletId, balance.value());
    }
}

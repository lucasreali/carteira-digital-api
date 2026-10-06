package com.example.wallet.wallets.create;

import com.example.wallet.wallets.domain.Wallet;
import java.math.BigDecimal;
import java.util.UUID;

public record CreateWalletResponse(UUID id, UUID ownerId, BigDecimal balance) {

    static CreateWalletResponse from(Wallet wallet) {
        return new CreateWalletResponse(wallet.id(), wallet.ownerId(), wallet.balance().value());
    }
}

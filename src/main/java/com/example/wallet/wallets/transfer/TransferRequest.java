package com.example.wallet.wallets.transfer;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import java.math.BigDecimal;
import java.util.UUID;

record TransferRequest(@NotNull UUID targetWalletId, @NotNull @Positive BigDecimal amount) {

    TransferCommand toCommand(UUID sourceWalletId, UUID requesterId) {
        return new TransferCommand(sourceWalletId, requesterId, targetWalletId, amount);
    }
}

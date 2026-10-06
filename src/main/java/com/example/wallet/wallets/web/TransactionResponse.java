package com.example.wallet.wallets.web;

import com.example.wallet.wallets.domain.Transaction;
import java.math.BigDecimal;
import java.time.Instant;
import java.util.UUID;

public record TransactionResponse(
        UUID id,
        UUID walletId,
        String type,
        BigDecimal amount,
        BigDecimal balanceAfter,
        UUID counterpartyWalletId,
        Instant occurredAt) {

    public static TransactionResponse from(Transaction transaction) {
        return new TransactionResponse(
                transaction.id(),
                transaction.walletId(),
                transaction.type().name(),
                transaction.amount().value(),
                transaction.balanceAfter().value(),
                transaction.counterpartyWalletId(),
                transaction.occurredAt());
    }
}

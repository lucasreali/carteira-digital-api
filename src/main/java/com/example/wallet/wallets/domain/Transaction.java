package com.example.wallet.wallets.domain;

import java.time.Instant;
import java.util.UUID;

/**
 * An immutable ledger entry of one wallet.
 *
 * @param counterpartyWalletId the other wallet of a transfer; {@code null} for deposits and withdrawals
 */
public record Transaction(
        UUID id,
        UUID walletId,
        TransactionType type,
        Money amount,
        Money balanceAfter,
        UUID counterpartyWalletId,
        Instant occurredAt) {}

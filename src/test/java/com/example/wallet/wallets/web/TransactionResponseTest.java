package com.example.wallet.wallets.web;

import static org.assertj.core.api.Assertions.assertThat;

import com.example.wallet.wallets.domain.Money;
import com.example.wallet.wallets.domain.Transaction;
import com.example.wallet.wallets.domain.TransactionType;
import java.math.BigDecimal;
import java.time.Instant;
import java.util.UUID;
import org.junit.jupiter.api.Test;

class TransactionResponseTest {

    @Test
    void mapsEveryTransactionField() {
        var transaction = new Transaction(
                UUID.randomUUID(),
                UUID.randomUUID(),
                TransactionType.TRANSFER_OUT,
                Money.of("30"),
                Money.of("70.5"),
                UUID.randomUUID(),
                Instant.parse("2026-10-05T12:00:00Z"));

        var response = TransactionResponse.from(transaction);

        assertThat(response.id()).isEqualTo(transaction.id());
        assertThat(response.walletId()).isEqualTo(transaction.walletId());
        assertThat(response.type()).isEqualTo("TRANSFER_OUT");
        assertThat(response.amount()).isEqualTo(new BigDecimal("30.00"));
        assertThat(response.balanceAfter()).isEqualTo(new BigDecimal("70.50"));
        assertThat(response.counterpartyWalletId()).isEqualTo(transaction.counterpartyWalletId());
        assertThat(response.occurredAt()).isEqualTo(transaction.occurredAt());
    }

    @Test
    void keepsCounterpartyNullForDeposits() {
        var transaction = new Transaction(
                UUID.randomUUID(), UUID.randomUUID(), TransactionType.DEPOSIT,
                Money.of("1"), Money.of("1"), null, Instant.parse("2026-10-05T12:00:00Z"));

        var response = TransactionResponse.from(transaction);

        assertThat(response.counterpartyWalletId()).isNull();
    }
}

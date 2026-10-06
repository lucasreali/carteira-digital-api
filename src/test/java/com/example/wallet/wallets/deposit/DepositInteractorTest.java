package com.example.wallet.wallets.deposit;

import static org.assertj.core.api.Assertions.assertThat;

import com.example.wallet.wallets.domain.Money;
import com.example.wallet.wallets.domain.TransactionType;
import com.example.wallet.wallets.domain.Wallet;
import java.math.BigDecimal;
import java.time.Clock;
import java.time.Instant;
import java.time.ZoneOffset;
import java.util.UUID;
import org.junit.jupiter.api.Test;

class DepositInteractorTest {

    private static final Instant NOW = Instant.parse("2026-10-05T12:00:00Z");

    private final UUID ownerId = UUID.randomUUID();
    private final Wallet wallet = Wallet.rehydrate(UUID.randomUUID(), ownerId, Money.of("5.00"), 0L);
    private final InMemoryWallets wallets = new InMemoryWallets().with(wallet);
    private final DepositUseCase useCase = new DepositInteractor(wallets, Clock.fixed(NOW, ZoneOffset.UTC));

    @Test
    void depositIncreasesBalanceAndPersistsDepositTransaction() {
        var command = new DepositCommand(wallet.id(), ownerId, new BigDecimal("10.50"));

        var transaction = useCase.deposit(command);

        assertThat(transaction.type()).isEqualTo(TransactionType.DEPOSIT);
        assertThat(transaction.walletId()).isEqualTo(wallet.id());
        assertThat(transaction.amount()).isEqualTo(Money.of("10.50"));
        assertThat(transaction.balanceAfter()).isEqualTo(Money.of("15.50"));
        assertThat(transaction.occurredAt()).isEqualTo(NOW);
        assertThat(wallets.findById(wallet.id()).orElseThrow().balance()).isEqualTo(Money.of("15.50"));
        assertThat(wallets.savedTransactions).containsExactly(transaction);
    }
}

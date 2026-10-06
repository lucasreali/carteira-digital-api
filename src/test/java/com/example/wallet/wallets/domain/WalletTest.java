package com.example.wallet.wallets.domain;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import java.time.Instant;
import java.util.UUID;
import org.junit.jupiter.api.Test;

class WalletTest {

    private static final Instant NOW = Instant.parse("2026-10-05T12:00:00Z");
    private final UUID ownerId = UUID.randomUUID();

    private Wallet walletWithBalance(String balance) {
        return Wallet.rehydrate(UUID.randomUUID(), ownerId, Money.of(balance), 3L);
    }

    @Test
    void openedWalletIsNewWithZeroBalanceAndNoPendingTransactions() {
        var wallet = Wallet.open(ownerId);

        assertThat(wallet.id()).isNotNull();
        assertThat(wallet.ownerId()).isEqualTo(ownerId);
        assertThat(wallet.balance()).isEqualTo(Money.zero());
        assertThat(wallet.version()).isNull();
        assertThat(wallet.pendingTransactions()).isEmpty();
    }

    @Test
    void eachOpenedWalletGetsItsOwnId() {
        assertThat(Wallet.open(ownerId).id()).isNotEqualTo(Wallet.open(ownerId).id());
    }

    @Test
    void rehydratedWalletKeepsPersistedState() {
        var id = UUID.randomUUID();

        var wallet = Wallet.rehydrate(id, ownerId, Money.of("42.10"), 7L);

        assertThat(wallet.id()).isEqualTo(id);
        assertThat(wallet.ownerId()).isEqualTo(ownerId);
        assertThat(wallet.balance()).isEqualTo(Money.of("42.10"));
        assertThat(wallet.version()).isEqualTo(7L);
        assertThat(wallet.pendingTransactions()).isEmpty();
    }

    @Test
    void depositIncreasesBalanceAndRecordsPendingTransaction() {
        var wallet = walletWithBalance("10.00");

        var transaction = wallet.deposit(Money.of("5.50"), NOW);

        assertThat(wallet.balance()).isEqualTo(Money.of("15.50"));
        assertThat(transaction.id()).isNotNull();
        assertThat(transaction.walletId()).isEqualTo(wallet.id());
        assertThat(transaction.type()).isEqualTo(TransactionType.DEPOSIT);
        assertThat(transaction.amount()).isEqualTo(Money.of("5.50"));
        assertThat(transaction.balanceAfter()).isEqualTo(Money.of("15.50"));
        assertThat(transaction.counterpartyWalletId()).isNull();
        assertThat(transaction.occurredAt()).isEqualTo(NOW);
        assertThat(wallet.pendingTransactions()).containsExactly(transaction);
    }

    @Test
    void depositRejectsZeroAmount() {
        var wallet = walletWithBalance("10.00");

        assertThatThrownBy(() -> wallet.deposit(Money.zero(), NOW)).isInstanceOf(InvalidAmountException.class);
        assertThat(wallet.balance()).isEqualTo(Money.of("10.00"));
        assertThat(wallet.pendingTransactions()).isEmpty();
    }

    @Test
    void pendingTransactionsAreUnmodifiableAndAccumulateInOrder() {
        var wallet = walletWithBalance("0.00");

        var first = wallet.deposit(Money.of("1.00"), NOW);
        var second = wallet.deposit(Money.of("2.00"), NOW.plusSeconds(1));

        assertThat(wallet.pendingTransactions()).containsExactly(first, second);
        assertThatThrownBy(() -> wallet.pendingTransactions().clear())
                .isInstanceOf(UnsupportedOperationException.class);
    }

    @Test
    void clearingPendingTransactionsKeepsBalance() {
        var wallet = walletWithBalance("0.00");
        wallet.deposit(Money.of("1.00"), NOW);

        wallet.clearPendingTransactions();

        assertThat(wallet.pendingTransactions()).isEmpty();
        assertThat(wallet.balance()).isEqualTo(Money.of("1.00"));
    }
}

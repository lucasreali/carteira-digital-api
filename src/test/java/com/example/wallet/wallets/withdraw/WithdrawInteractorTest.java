package com.example.wallet.wallets.withdraw;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.catchThrowable;

import com.example.wallet.wallets.domain.Money;
import com.example.wallet.wallets.domain.TransactionType;
import com.example.wallet.wallets.domain.Wallet;
import com.example.wallet.wallets.domain.WalletAccessDeniedException;
import com.example.wallet.wallets.domain.WalletNotFoundException;
import java.math.BigDecimal;
import java.time.Clock;
import java.time.Instant;
import java.time.ZoneOffset;
import java.util.UUID;
import org.junit.jupiter.api.Test;

class WithdrawInteractorTest {

    private static final Instant NOW = Instant.parse("2026-10-05T12:00:00Z");

    private final UUID ownerId = UUID.randomUUID();
    private final Wallet wallet = Wallet.rehydrate(UUID.randomUUID(), ownerId, Money.of("100.00"), 0L);
    private final InMemoryWallets wallets = new InMemoryWallets().with(wallet);
    private final WithdrawUseCase withdraw = new WithdrawInteractor(wallets, Clock.fixed(NOW, ZoneOffset.UTC));

    @Test
    void decreasesBalanceAndPersistsWithdrawal() {
        var command = new WithdrawCommand(wallet.id(), ownerId, new BigDecimal("30.00"));

        var transaction = withdraw.withdraw(command);

        assertThat(transaction.type()).isEqualTo(TransactionType.WITHDRAWAL);
        assertThat(transaction.walletId()).isEqualTo(wallet.id());
        assertThat(transaction.amount()).isEqualTo(Money.of("30.00"));
        assertThat(transaction.balanceAfter()).isEqualTo(Money.of("70.00"));
        assertThat(transaction.occurredAt()).isEqualTo(NOW);
        assertThat(wallets.findById(wallet.id()).orElseThrow().balance()).isEqualTo(Money.of("70.00"));
        assertThat(wallets.savedTransactions).containsExactly(transaction);
    }

    @Test
    void rejectsUnknownWallet() {
        var command = new WithdrawCommand(UUID.randomUUID(), ownerId, new BigDecimal("10.00"));

        var thrown = catchThrowable(() -> withdraw.withdraw(command));

        assertThat(thrown).isInstanceOf(WalletNotFoundException.class);
        assertThat(wallets.saveCalls).isZero();
    }

    @Test
    void rejectsWithdrawalByNonOwnerWithoutSaving() {
        var command = new WithdrawCommand(wallet.id(), UUID.randomUUID(), new BigDecimal("10.00"));

        var thrown = catchThrowable(() -> withdraw.withdraw(command));

        assertThat(thrown).isInstanceOf(WalletAccessDeniedException.class);
        assertThat(wallets.saveCalls).isZero();
        assertThat(wallet.balance()).isEqualTo(Money.of("100.00"));
    }
}

package com.example.wallet.wallets.domain;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import com.example.wallet.shared.domain.BusinessRuleException;
import com.example.wallet.shared.domain.ForbiddenException;
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
    void withdrawDecreasesBalanceAndRecordsPendingTransaction() {
        var wallet = walletWithBalance("10.00");

        var transaction = wallet.withdraw(Money.of("4.00"), NOW);

        assertThat(wallet.balance()).isEqualTo(Money.of("6.00"));
        assertThat(transaction.walletId()).isEqualTo(wallet.id());
        assertThat(transaction.type()).isEqualTo(TransactionType.WITHDRAWAL);
        assertThat(transaction.amount()).isEqualTo(Money.of("4.00"));
        assertThat(transaction.balanceAfter()).isEqualTo(Money.of("6.00"));
        assertThat(transaction.counterpartyWalletId()).isNull();
        assertThat(transaction.occurredAt()).isEqualTo(NOW);
        assertThat(wallet.pendingTransactions()).containsExactly(transaction);
    }

    @Test
    void withdrawAllowsEmptyingTheWallet() {
        var wallet = walletWithBalance("10.00");

        wallet.withdraw(Money.of("10.00"), NOW);

        assertThat(wallet.balance()).isEqualTo(Money.zero());
    }

    @Test
    void rejectsWithdrawalAboveBalance() {
        var wallet = walletWithBalance("10.00");

        assertThatThrownBy(() -> wallet.withdraw(Money.of("10.01"), NOW))
                .isInstanceOf(InsufficientFundsException.class)
                .isInstanceOf(BusinessRuleException.class)
                .hasMessageContaining("Insufficient funds");
        assertThat(wallet.balance()).isEqualTo(Money.of("10.00"));
        assertThat(wallet.pendingTransactions()).isEmpty();
    }

    @Test
    void withdrawRejectsZeroAmount() {
        var wallet = walletWithBalance("10.00");

        assertThatThrownBy(() -> wallet.withdraw(Money.zero(), NOW)).isInstanceOf(InvalidAmountException.class);
        assertThat(wallet.pendingTransactions()).isEmpty();
    }

    @Test
    void transferMovesMoneyAndRecordsMirroredTransactionsOnBothWallets() {
        var source = walletWithBalance("100.00");
        var target = Wallet.rehydrate(UUID.randomUUID(), UUID.randomUUID(), Money.of("5.00"), 1L);

        var out = source.transferTo(target, Money.of("30.00"), NOW);

        assertThat(source.balance()).isEqualTo(Money.of("70.00"));
        assertThat(target.balance()).isEqualTo(Money.of("35.00"));
        assertThat(out.walletId()).isEqualTo(source.id());
        assertThat(out.type()).isEqualTo(TransactionType.TRANSFER_OUT);
        assertThat(out.amount()).isEqualTo(Money.of("30.00"));
        assertThat(out.balanceAfter()).isEqualTo(Money.of("70.00"));
        assertThat(out.counterpartyWalletId()).isEqualTo(target.id());
        assertThat(out.occurredAt()).isEqualTo(NOW);
        assertThat(source.pendingTransactions()).containsExactly(out);
        assertThat(target.pendingTransactions()).singleElement().satisfies(in -> {
            assertThat(in.id()).isNotEqualTo(out.id());
            assertThat(in.walletId()).isEqualTo(target.id());
            assertThat(in.type()).isEqualTo(TransactionType.TRANSFER_IN);
            assertThat(in.amount()).isEqualTo(Money.of("30.00"));
            assertThat(in.balanceAfter()).isEqualTo(Money.of("35.00"));
            assertThat(in.counterpartyWalletId()).isEqualTo(source.id());
            assertThat(in.occurredAt()).isEqualTo(NOW);
        });
    }

    @Test
    void rejectsTransferAboveBalanceWithoutTouchingEitherWallet() {
        var source = walletWithBalance("10.00");
        var target = walletWithBalance("0.00");

        assertThatThrownBy(() -> source.transferTo(target, Money.of("10.01"), NOW))
                .isInstanceOf(InsufficientFundsException.class);
        assertThat(source.balance()).isEqualTo(Money.of("10.00"));
        assertThat(target.balance()).isEqualTo(Money.zero());
        assertThat(source.pendingTransactions()).isEmpty();
        assertThat(target.pendingTransactions()).isEmpty();
    }

    @Test
    void rejectsTransferToTheSameWallet() {
        var source = walletWithBalance("10.00");
        var sameWalletLoadedAgain = Wallet.rehydrate(source.id(), ownerId, Money.of("10.00"), 3L);

        assertThatThrownBy(() -> source.transferTo(sameWalletLoadedAgain, Money.of("1.00"), NOW))
                .isInstanceOf(SameWalletTransferException.class)
                .isInstanceOf(BusinessRuleException.class);
        assertThatThrownBy(() -> source.transferTo(source, Money.of("1.00"), NOW))
                .isInstanceOf(SameWalletTransferException.class);
        assertThat(source.balance()).isEqualTo(Money.of("10.00"));
        assertThat(source.pendingTransactions()).isEmpty();
    }

    @Test
    void transferRejectsZeroAmount() {
        var source = walletWithBalance("10.00");
        var target = walletWithBalance("0.00");

        assertThatThrownBy(() -> source.transferTo(target, Money.zero(), NOW))
                .isInstanceOf(InvalidAmountException.class);
        assertThat(target.pendingTransactions()).isEmpty();
    }

    @Test
    void ownerPassesOwnershipCheck() {
        var wallet = walletWithBalance("1.00");

        wallet.ensureOwnedBy(ownerId);
    }

    @Test
    void rejectsAccessByAnotherUserWithoutRevealingTheOwner() {
        var wallet = walletWithBalance("1.00");

        assertThatThrownBy(() -> wallet.ensureOwnedBy(UUID.randomUUID()))
                .isInstanceOf(WalletAccessDeniedException.class)
                .isInstanceOf(ForbiddenException.class)
                .message()
                .doesNotContain(ownerId.toString())
                .doesNotContain("1.00");
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

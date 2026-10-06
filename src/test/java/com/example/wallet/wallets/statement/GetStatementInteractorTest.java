package com.example.wallet.wallets.statement;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import com.example.wallet.wallets.domain.Money;
import com.example.wallet.wallets.domain.Wallet;
import com.example.wallet.wallets.domain.WalletAccessDeniedException;
import com.example.wallet.wallets.domain.WalletNotFoundException;
import java.time.Instant;
import java.util.List;
import java.util.UUID;
import org.junit.jupiter.api.Test;

class GetStatementInteractorTest {

    private final InMemoryWallets wallets = new InMemoryWallets();
    private final GetStatementUseCase useCase = new GetStatementInteractor(wallets, wallets);

    private final UUID ownerId = UUID.randomUUID();

    @Test
    void returnsHistoryForOwnerInTheOrderGivenByThePort() {
        var wallet = Wallet.open(ownerId);
        var older = wallet.deposit(Money.of("10.00"), Instant.parse("2026-01-01T10:00:00Z"));
        var newer = wallet.withdraw(Money.of("3.00"), Instant.parse("2026-01-02T10:00:00Z"));
        wallets.save(wallet);
        wallets.givenHistory(wallet.id(), List.of(newer, older));

        var statement = useCase.getStatement(new GetStatementQuery(wallet.id(), ownerId));

        assertThat(statement).containsExactly(newer, older);
    }

    @Test
    void returnsEmptyListWhenWalletHasNoTransactions() {
        var wallet = Wallet.open(ownerId);
        wallets.save(wallet);

        var statement = useCase.getStatement(new GetStatementQuery(wallet.id(), ownerId));

        assertThat(statement).isEmpty();
    }

    @Test
    void rejectsUnknownWallet() {
        var unknownId = UUID.randomUUID();

        assertThatThrownBy(() -> useCase.getStatement(new GetStatementQuery(unknownId, ownerId)))
                .isInstanceOf(WalletNotFoundException.class);

        assertThat(wallets.historyQueries).isEmpty();
    }

    @Test
    void rejectsRequesterWhoDoesNotOwnTheWallet() {
        var wallet = Wallet.open(ownerId);
        wallets.save(wallet);
        var stranger = UUID.randomUUID();

        assertThatThrownBy(() -> useCase.getStatement(new GetStatementQuery(wallet.id(), stranger)))
                .isInstanceOf(WalletAccessDeniedException.class);

        assertThat(wallets.historyQueries).isEmpty();
    }
}

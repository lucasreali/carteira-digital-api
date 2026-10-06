package com.example.wallet.wallets.statement;

import static org.assertj.core.api.Assertions.assertThat;

import com.example.wallet.wallets.domain.Money;
import com.example.wallet.wallets.domain.Wallet;
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
}

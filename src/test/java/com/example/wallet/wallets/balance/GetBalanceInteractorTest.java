package com.example.wallet.wallets.balance;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.catchThrowable;

import com.example.wallet.wallets.domain.Money;
import com.example.wallet.wallets.domain.Wallet;
import com.example.wallet.wallets.domain.WalletNotFoundException;
import java.util.UUID;
import org.junit.jupiter.api.Test;

class GetBalanceInteractorTest {

    private final InMemoryWallets wallets = new InMemoryWallets();
    private final GetBalanceUseCase useCase = new GetBalanceInteractor(wallets);

    private final UUID ownerId = UUID.randomUUID();

    @Test
    void returnsBalanceOfOwnersWallet() {
        var wallet = Wallet.rehydrate(UUID.randomUUID(), ownerId, Money.of("42.50"), 3L);
        wallets.add(wallet);

        var balance = useCase.getBalance(new GetBalanceQuery(wallet.id(), ownerId));

        assertThat(balance).isEqualTo(Money.of("42.50"));
        assertThat(wallets.saveCalls).isZero();
    }

    @Test
    void rejectsUnknownWallet() {
        var unknownId = UUID.randomUUID();

        var thrown = catchThrowable(() -> useCase.getBalance(new GetBalanceQuery(unknownId, ownerId)));

        assertThat(thrown).isInstanceOf(WalletNotFoundException.class).hasMessageContaining(unknownId.toString());
    }
}

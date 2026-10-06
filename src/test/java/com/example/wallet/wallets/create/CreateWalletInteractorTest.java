package com.example.wallet.wallets.create;

import static org.assertj.core.api.Assertions.assertThat;

import com.example.wallet.wallets.domain.Money;
import java.util.UUID;
import org.junit.jupiter.api.Test;

class CreateWalletInteractorTest {

    private final InMemoryWallets wallets = new InMemoryWallets();
    private final CreateWalletUseCase useCase = new CreateWalletInteractor(wallets);

    @Test
    void opensAndSavesZeroBalanceWalletForOwner() {
        var ownerId = UUID.randomUUID();

        var wallet = useCase.create(new CreateWalletCommand(ownerId));

        assertThat(wallet.ownerId()).isEqualTo(ownerId);
        assertThat(wallet.balance()).isEqualTo(Money.zero());
        assertThat(wallets.saved).containsEntry(wallet.id(), wallet);
    }
}

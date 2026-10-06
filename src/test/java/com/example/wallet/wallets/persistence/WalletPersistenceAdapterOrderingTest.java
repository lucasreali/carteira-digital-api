package com.example.wallet.wallets.persistence;

import static org.mockito.ArgumentMatchers.argThat;
import static org.mockito.Mockito.inOrder;
import static org.mockito.Mockito.mock;

import com.example.wallet.wallets.domain.Money;
import com.example.wallet.wallets.domain.Wallet;
import java.util.UUID;
import org.junit.jupiter.api.Test;

class WalletPersistenceAdapterOrderingTest {

    private final WalletJpaRepository wallets = mock(WalletJpaRepository.class);
    private final WalletPersistenceAdapter adapter =
            new WalletPersistenceAdapter(wallets, mock(TransactionJpaRepository.class));

    private static Wallet wallet(String id) {
        return Wallet.rehydrate(UUID.fromString(id), UUID.randomUUID(), Money.of("10.00"), 0L);
    }

    @Test
    void writesWalletsInIdOrderRegardlessOfArgumentOrderSoOppositeTransfersLockRowsAlike() {
        var lower = wallet("00000000-0000-0000-0000-000000000001");
        var higher = wallet("00000000-0000-0000-0000-000000000002");

        adapter.save(higher, lower);

        var order = inOrder(wallets);
        order.verify(wallets).save(argThat(entity -> entity.toDomain().id().equals(lower.id())));
        order.verify(wallets).save(argThat(entity -> entity.toDomain().id().equals(higher.id())));
    }
}

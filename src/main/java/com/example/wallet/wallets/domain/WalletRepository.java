package com.example.wallet.wallets.domain;

import java.util.Optional;
import java.util.UUID;

public interface WalletRepository {

    Optional<Wallet> findById(UUID id);

    boolean existsByOwnerId(UUID ownerId);

    /**
     * Atomically persists the wallets and their pending transactions, then clears the pending lists. Fails with
     * Spring's {@code OptimisticLockingFailureException} when a wallet was changed since it was loaded. The saved
     * instances keep their old version: reload a wallet before changing it again.
     */
    void save(Wallet... wallets);
}

package com.example.wallet.wallets.create;

import com.example.wallet.wallets.domain.Wallet;
import com.example.wallet.wallets.domain.WalletRepository;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;

class InMemoryWallets implements WalletRepository {

    final Map<UUID, Wallet> saved = new LinkedHashMap<>();

    @Override
    public Optional<Wallet> findById(UUID id) {
        return Optional.ofNullable(saved.get(id));
    }

    @Override
    public boolean existsByOwnerId(UUID ownerId) {
        return saved.values().stream().anyMatch(wallet -> wallet.ownerId().equals(ownerId));
    }

    @Override
    public void save(Wallet... wallets) {
        for (Wallet wallet : wallets) {
            saved.put(wallet.id(), wallet);
        }
    }
}

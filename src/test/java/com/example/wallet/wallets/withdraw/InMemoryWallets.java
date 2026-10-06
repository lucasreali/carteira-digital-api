package com.example.wallet.wallets.withdraw;

import com.example.wallet.wallets.domain.Transaction;
import com.example.wallet.wallets.domain.Wallet;
import com.example.wallet.wallets.domain.WalletRepository;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;

class InMemoryWallets implements WalletRepository {

    private final Map<UUID, Wallet> wallets = new HashMap<>();
    final List<Transaction> savedTransactions = new ArrayList<>();
    int saveCalls;

    InMemoryWallets with(Wallet wallet) {
        wallets.put(wallet.id(), wallet);
        return this;
    }

    @Override
    public Optional<Wallet> findById(UUID id) {
        return Optional.ofNullable(wallets.get(id));
    }

    @Override
    public boolean existsByOwnerId(UUID ownerId) {
        return wallets.values().stream().anyMatch(w -> w.ownerId().equals(ownerId));
    }

    @Override
    public void save(Wallet... toSave) {
        saveCalls++;
        for (var wallet : toSave) {
            savedTransactions.addAll(wallet.pendingTransactions());
            wallet.clearPendingTransactions();
            wallets.put(wallet.id(), wallet);
        }
    }
}

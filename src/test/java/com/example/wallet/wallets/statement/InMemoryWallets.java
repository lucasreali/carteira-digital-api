package com.example.wallet.wallets.statement;

import com.example.wallet.wallets.domain.Transaction;
import com.example.wallet.wallets.domain.TransactionHistory;
import com.example.wallet.wallets.domain.Wallet;
import com.example.wallet.wallets.domain.WalletRepository;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;

class InMemoryWallets implements WalletRepository, TransactionHistory {

    private final Map<UUID, Wallet> wallets = new HashMap<>();
    private final Map<UUID, List<Transaction>> history = new HashMap<>();
    final List<UUID> historyQueries = new ArrayList<>();

    @Override
    public Optional<Wallet> findById(UUID id) {
        return Optional.ofNullable(wallets.get(id));
    }

    @Override
    public boolean existsByOwnerId(UUID ownerId) {
        return wallets.values().stream().anyMatch(wallet -> wallet.ownerId().equals(ownerId));
    }

    @Override
    public void save(Wallet... toSave) {
        for (Wallet wallet : toSave) {
            wallets.put(wallet.id(), wallet);
        }
    }

    /** Stores the history exactly in the given order, as the real adapter would return it (newest first). */
    void givenHistory(UUID walletId, List<Transaction> newestFirst) {
        history.put(walletId, List.copyOf(newestFirst));
    }

    @Override
    public List<Transaction> findByWalletId(UUID walletId) {
        historyQueries.add(walletId);
        return history.getOrDefault(walletId, List.of());
    }
}

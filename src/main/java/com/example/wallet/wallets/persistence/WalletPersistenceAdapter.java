package com.example.wallet.wallets.persistence;

import com.example.wallet.wallets.domain.Transaction;
import com.example.wallet.wallets.domain.TransactionHistory;
import com.example.wallet.wallets.domain.Wallet;
import com.example.wallet.wallets.domain.WalletRepository;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

@Repository
class WalletPersistenceAdapter implements WalletRepository, TransactionHistory {

    private final WalletJpaRepository wallets;
    private final TransactionJpaRepository transactions;

    WalletPersistenceAdapter(WalletJpaRepository wallets, TransactionJpaRepository transactions) {
        this.wallets = wallets;
        this.transactions = transactions;
    }

    @Override
    @Transactional(readOnly = true)
    public Optional<Wallet> findById(UUID id) {
        return wallets.findById(id).map(WalletJpaEntity::toDomain);
    }

    @Override
    @Transactional(readOnly = true)
    public boolean existsByOwnerId(UUID ownerId) {
        return wallets.existsByOwnerId(ownerId);
    }

    /** Version null inserts; otherwise merge checks the version and fails on a stale wallet. */
    @Override
    @Transactional
    public void save(Wallet... toSave) {
        for (Wallet wallet : toSave) {
            wallets.save(WalletJpaEntity.from(wallet));
            transactions.saveAll(wallet.pendingTransactions().stream().map(TransactionJpaEntity::from).toList());
        }
        for (Wallet wallet : toSave) {
            wallet.clearPendingTransactions();
        }
    }

    @Override
    @Transactional(readOnly = true)
    public List<Transaction> findByWalletId(UUID walletId) {
        return transactions.findByWalletIdOrderByOccurredAtDesc(walletId).stream()
                .map(TransactionJpaEntity::toDomain)
                .toList();
    }
}

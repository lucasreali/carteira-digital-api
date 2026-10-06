package com.example.wallet.wallets.persistence;

import com.example.wallet.wallets.domain.Money;
import com.example.wallet.wallets.domain.Transaction;
import com.example.wallet.wallets.domain.TransactionType;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.Id;
import jakarta.persistence.Index;
import jakarta.persistence.Table;
import java.math.BigDecimal;
import java.time.Instant;
import java.util.UUID;
import org.springframework.data.domain.Persistable;

@Entity
@Table(name = "wallet_transactions", indexes = @Index(name = "idx_wallet_transactions_wallet_id", columnList = "wallet_id"))
class TransactionJpaEntity implements Persistable<UUID> {

    @Id
    private UUID id;

    @Column(name = "wallet_id", nullable = false)
    private UUID walletId;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private TransactionType type;

    @Column(nullable = false, precision = 19, scale = 2)
    private BigDecimal amount;

    @Column(name = "balance_after", nullable = false, precision = 19, scale = 2)
    private BigDecimal balanceAfter;

    @Column(name = "counterparty_wallet_id")
    private UUID counterpartyWalletId;

    @Column(name = "occurred_at", nullable = false)
    private Instant occurredAt;

    protected TransactionJpaEntity() {}

    static TransactionJpaEntity from(Transaction transaction) {
        var entity = new TransactionJpaEntity();
        entity.id = transaction.id();
        entity.walletId = transaction.walletId();
        entity.type = transaction.type();
        entity.amount = transaction.amount().value();
        entity.balanceAfter = transaction.balanceAfter().value();
        entity.counterpartyWalletId = transaction.counterpartyWalletId();
        entity.occurredAt = transaction.occurredAt();
        return entity;
    }

    @Override
    public UUID getId() {
        return id;
    }

    /** The ledger is append-only and ids are assigned by the domain: always persist, never merge. */
    @Override
    public boolean isNew() {
        return true;
    }

    Transaction toDomain() {
        return new Transaction(
                id, walletId, type, Money.of(amount), Money.of(balanceAfter), counterpartyWalletId, occurredAt);
    }
}

package com.example.wallet.wallets.domain;

import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

/**
 * Wallet aggregate. Every operation changes the balance and appends a {@link Transaction} to the pending list,
 * which the repository persists together with the wallet.
 */
public final class Wallet {

    private final UUID id;
    private final UUID ownerId;
    private final Long version;
    private Money balance;
    private final List<Transaction> pending = new ArrayList<>();

    private Wallet(UUID id, UUID ownerId, Money balance, Long version) {
        this.id = id;
        this.ownerId = ownerId;
        this.balance = balance;
        this.version = version;
    }

    /** A brand-new wallet: random id, zero balance, no version yet. */
    public static Wallet open(UUID ownerId) {
        return new Wallet(UUID.randomUUID(), ownerId, Money.zero(), null);
    }

    /** Rebuilds a persisted wallet. */
    public static Wallet rehydrate(UUID id, UUID ownerId, Money balance, Long version) {
        return new Wallet(id, ownerId, balance, version);
    }

    public void ensureOwnedBy(UUID userId) {
        if (!ownerId.equals(userId)) {
            throw new WalletAccessDeniedException();
        }
    }

    public Transaction deposit(Money amount, Instant occurredAt) {
        amount.requirePositive();
        balance = balance.plus(amount);
        return record(TransactionType.DEPOSIT, amount, null, occurredAt);
    }

    public Transaction withdraw(Money amount, Instant occurredAt) {
        debit(amount);
        return record(TransactionType.WITHDRAWAL, amount, null, occurredAt);
    }

    /**
     * Moves money to {@code target}. Records TRANSFER_OUT here and TRANSFER_IN on the target (each pointing at the
     * other wallet) and returns the TRANSFER_OUT. Both wallets must then be saved together.
     */
    public Transaction transferTo(Wallet target, Money amount, Instant occurredAt) {
        if (target.id.equals(id)) {
            throw new SameWalletTransferException();
        }
        var targetBalance = target.balance.plus(amount); // may throw: compute before touching the source
        debit(amount);
        target.balance = targetBalance;
        target.record(TransactionType.TRANSFER_IN, amount, id, occurredAt);
        return record(TransactionType.TRANSFER_OUT, amount, target.id, occurredAt);
    }

    private void debit(Money amount) {
        amount.requirePositive();
        if (amount.isGreaterThan(balance)) {
            throw new InsufficientFundsException();
        }
        balance = balance.minus(amount);
    }

    private Transaction record(TransactionType type, Money amount, UUID counterpartyWalletId, Instant occurredAt) {
        var transaction = new Transaction(
                UUID.randomUUID(), id, type, amount, balance, counterpartyWalletId, occurredAt);
        pending.add(transaction);
        return transaction;
    }

    /** Transactions produced since the wallet was loaded or last saved, oldest first. */
    public List<Transaction> pendingTransactions() {
        return List.copyOf(pending);
    }

    /** Called by the repository once the pending transactions are persisted. */
    public void clearPendingTransactions() {
        pending.clear();
    }

    public UUID id() {
        return id;
    }

    public UUID ownerId() {
        return ownerId;
    }

    public Money balance() {
        return balance;
    }

    /** Optimistic-lock version; {@code null} for a wallet that was never saved. */
    public Long version() {
        return version;
    }
}

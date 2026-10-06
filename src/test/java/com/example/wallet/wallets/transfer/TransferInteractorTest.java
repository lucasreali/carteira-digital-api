package com.example.wallet.wallets.transfer;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.tuple;

import com.example.wallet.wallets.domain.Money;
import com.example.wallet.wallets.domain.Transaction;
import com.example.wallet.wallets.domain.TransactionType;
import com.example.wallet.wallets.domain.Wallet;
import com.example.wallet.wallets.domain.WalletRepository;
import java.math.BigDecimal;
import java.time.Clock;
import java.time.Instant;
import java.time.ZoneOffset;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;
import org.junit.jupiter.api.Test;

class TransferInteractorTest {

    private static final Instant NOW = Instant.parse("2026-10-05T12:00:00Z");

    private final InMemoryWallets wallets = new InMemoryWallets();
    private final TransferInteractor interactor =
            new TransferInteractor(wallets, Clock.fixed(NOW, ZoneOffset.UTC));

    private final UUID alice = UUID.randomUUID();
    private final UUID bob = UUID.randomUUID();

    private Wallet stored(UUID ownerId, String balance) {
        Wallet wallet = Wallet.rehydrate(UUID.randomUUID(), ownerId, Money.of(balance), 0L);
        wallets.put(wallet);
        return wallet;
    }

    @Test
    void movesMoneyAndSavesBothWalletsInOneCall() {
        Wallet source = stored(alice, "100.00");
        Wallet target = stored(bob, "5.00");

        Transaction out = interactor.transfer(
                new TransferCommand(source.id(), alice, target.id(), new BigDecimal("30.00")));

        assertThat(out.type()).isEqualTo(TransactionType.TRANSFER_OUT);
        assertThat(out.walletId()).isEqualTo(source.id());
        assertThat(out.counterpartyWalletId()).isEqualTo(target.id());
        assertThat(out.amount()).isEqualTo(Money.of("30.00"));
        assertThat(out.balanceAfter()).isEqualTo(Money.of("70.00"));
        assertThat(out.occurredAt()).isEqualTo(NOW);
        assertThat(wallets.saveCalls).hasSize(1);
        SavedCall call = wallets.saveCalls.getFirst();
        assertThat(call.wallets()).extracting(Wallet::id, Wallet::balance).containsExactly(
                tuple(source.id(), Money.of("70.00")),
                tuple(target.id(), Money.of("35.00")));
        assertThat(call.pending()).extracting(Transaction::walletId, Transaction::type).containsExactly(
                tuple(source.id(), TransactionType.TRANSFER_OUT),
                tuple(target.id(), TransactionType.TRANSFER_IN));
    }

    record SavedCall(List<Wallet> wallets, List<Transaction> pending) {
    }

    /** Hands out fresh copies, like a database would, and records every save call. */
    static class InMemoryWallets implements WalletRepository {

        private final Map<UUID, Wallet> byId = new HashMap<>();
        final List<SavedCall> saveCalls = new ArrayList<>();

        void put(Wallet wallet) {
            byId.put(wallet.id(), wallet);
        }

        @Override
        public Optional<Wallet> findById(UUID id) {
            return Optional.ofNullable(byId.get(id))
                    .map(w -> Wallet.rehydrate(w.id(), w.ownerId(), w.balance(), w.version()));
        }

        @Override
        public boolean existsByOwnerId(UUID ownerId) {
            return byId.values().stream().anyMatch(w -> w.ownerId().equals(ownerId));
        }

        @Override
        public void save(Wallet... toSave) {
            var pending = new ArrayList<Transaction>();
            for (Wallet wallet : toSave) {
                pending.addAll(wallet.pendingTransactions());
                wallet.clearPendingTransactions();
                put(wallet);
            }
            saveCalls.add(new SavedCall(List.of(toSave), pending));
        }
    }
}

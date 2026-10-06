package com.example.wallet.wallets.persistence;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import com.example.wallet.wallets.domain.Money;
import com.example.wallet.wallets.domain.Transaction;
import com.example.wallet.wallets.domain.TransactionType;
import com.example.wallet.wallets.domain.Wallet;
import java.time.Instant;
import java.util.UUID;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.data.jpa.test.autoconfigure.DataJpaTest;
import org.springframework.boot.jpa.test.autoconfigure.TestEntityManager;
import org.springframework.context.annotation.Import;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.dao.OptimisticLockingFailureException;

@DataJpaTest
@Import(WalletPersistenceAdapter.class)
class WalletPersistenceAdapterTest {

    private static final Instant T1 = Instant.parse("2026-10-05T10:00:00Z");
    private static final Instant T2 = Instant.parse("2026-10-05T11:00:00Z");
    private static final Instant T3 = Instant.parse("2026-10-05T12:00:00Z");

    @Autowired
    WalletPersistenceAdapter adapter;

    @Autowired
    TestEntityManager entityManager;

    @Autowired
    WalletJpaRepository walletJpaRepository;

    private void flushAndClear() {
        entityManager.flush();
        entityManager.clear();
    }

    private Wallet reload(UUID id) {
        return adapter.findById(id).orElseThrow();
    }

    @Test
    void savesNewWalletAndLoadsItBackWithInitialVersion() {
        var wallet = Wallet.open(UUID.randomUUID());

        adapter.save(wallet);
        flushAndClear();

        var loaded = reload(wallet.id());
        assertThat(loaded.ownerId()).isEqualTo(wallet.ownerId());
        assertThat(loaded.balance()).isEqualTo(Money.zero());
        assertThat(loaded.version()).isZero();
        assertThat(loaded.pendingTransactions()).isEmpty();
    }

    @Test
    void findByIdIsEmptyForUnknownWallet() {
        assertThat(adapter.findById(UUID.randomUUID())).isEmpty();
    }

    @Test
    void existsByOwnerIdReflectsSavedWallets() {
        var ownerId = UUID.randomUUID();
        adapter.save(Wallet.open(ownerId));
        flushAndClear();

        assertThat(adapter.existsByOwnerId(ownerId)).isTrue();
        assertThat(adapter.existsByOwnerId(UUID.randomUUID())).isFalse();
    }

    @Test
    void savePersistsPendingTransactionsClearsThemAndHistoryIsNewestFirst() {
        var wallet = Wallet.open(UUID.randomUUID());
        var deposit = wallet.deposit(Money.of("50.00"), T1);
        var withdrawal = wallet.withdraw(Money.of("20.00"), T2);

        adapter.save(wallet);
        flushAndClear();

        assertThat(wallet.pendingTransactions()).isEmpty();
        var reloaded = reload(wallet.id());
        assertThat(reloaded.balance()).isEqualTo(Money.of("30.00"));
        var secondDeposit = reloaded.deposit(Money.of("0.01"), T3);
        adapter.save(reloaded);
        flushAndClear();

        assertThat(reload(wallet.id()).version()).isEqualTo(1L);
        assertThat(adapter.findByWalletId(wallet.id())).containsExactly(secondDeposit, withdrawal, deposit);
    }

    @Test
    void savingATransferPersistsBothWalletsAndEachOneSeesOnlyItsOwnTransactions() {
        var source = Wallet.open(UUID.randomUUID());
        var target = Wallet.open(UUID.randomUUID());
        source.deposit(Money.of("10.00"), T1);
        var out = source.transferTo(target, Money.of("4.00"), T2);

        adapter.save(source, target);
        flushAndClear();

        assertThat(reload(source.id()).balance()).isEqualTo(Money.of("6.00"));
        assertThat(reload(target.id()).balance()).isEqualTo(Money.of("4.00"));
        assertThat(adapter.findByWalletId(source.id())).first().isEqualTo(out);
        assertThat(adapter.findByWalletId(target.id()))
                .singleElement()
                .extracting(Transaction::type, Transaction::counterpartyWalletId)
                .containsExactly(TransactionType.TRANSFER_IN, source.id());
    }

    @Test
    void savingAStaleWalletFailsWithOptimisticLock() {
        var wallet = Wallet.open(UUID.randomUUID());
        adapter.save(wallet);
        flushAndClear();
        var first = reload(wallet.id());
        var stale = reload(wallet.id());
        first.deposit(Money.of("1.00"), T1);
        adapter.save(first);
        flushAndClear();
        stale.deposit(Money.of("2.00"), T2);

        assertThatThrownBy(() -> {
                    adapter.save(stale);
                    entityManager.flush();
                })
                .isInstanceOf(OptimisticLockingFailureException.class);
    }

    @Test
    void enforcesOneWalletPerOwner() {
        var ownerId = UUID.randomUUID();
        adapter.save(Wallet.open(ownerId));
        adapter.save(Wallet.open(ownerId));

        assertThatThrownBy(() -> walletJpaRepository.flush()).isInstanceOf(DataIntegrityViolationException.class);
    }
}

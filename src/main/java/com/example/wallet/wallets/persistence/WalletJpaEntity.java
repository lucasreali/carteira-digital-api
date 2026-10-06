package com.example.wallet.wallets.persistence;

import com.example.wallet.wallets.domain.Money;
import com.example.wallet.wallets.domain.Wallet;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import jakarta.persistence.Version;
import java.math.BigDecimal;
import java.util.UUID;

@Entity
@Table(name = "wallets")
class WalletJpaEntity {

    @Id
    private UUID id;

    @Column(name = "owner_id", nullable = false, unique = true, updatable = false)
    private UUID ownerId;

    @Column(nullable = false, precision = 19, scale = 2)
    private BigDecimal balance;

    @Version
    private Long version;

    protected WalletJpaEntity() {}

    static WalletJpaEntity from(Wallet wallet) {
        var entity = new WalletJpaEntity();
        entity.id = wallet.id();
        entity.ownerId = wallet.ownerId();
        entity.balance = wallet.balance().value();
        entity.version = wallet.version();
        return entity;
    }

    Wallet toDomain() {
        return Wallet.rehydrate(id, ownerId, Money.of(balance), version);
    }
}

package com.example.wallet.wallets.persistence;

import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;

interface WalletJpaRepository extends JpaRepository<WalletJpaEntity, UUID> {

    boolean existsByOwnerId(UUID ownerId);
}

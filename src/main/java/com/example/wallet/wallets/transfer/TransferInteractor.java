package com.example.wallet.wallets.transfer;

import com.example.wallet.wallets.domain.Money;
import com.example.wallet.wallets.domain.Transaction;
import com.example.wallet.wallets.domain.Wallet;
import com.example.wallet.wallets.domain.WalletNotFoundException;
import com.example.wallet.wallets.domain.WalletRepository;
import java.time.Clock;
import java.time.Instant;
import java.util.UUID;

public class TransferInteractor implements TransferUseCase {

    private final WalletRepository wallets;
    private final Clock clock;

    public TransferInteractor(WalletRepository wallets, Clock clock) {
        this.wallets = wallets;
        this.clock = clock;
    }

    @Override
    public Transaction transfer(TransferCommand command) {
        Wallet source = load(command.sourceWalletId());
        source.ensureOwnedBy(command.requesterId());
        Wallet target = load(command.targetWalletId()); // any user's wallet may receive
        Transaction out = source.transferTo(target, Money.of(command.amount()), Instant.now(clock));
        wallets.save(source, target);
        return out;
    }

    private Wallet load(UUID walletId) {
        return wallets.findById(walletId).orElseThrow(() -> new WalletNotFoundException(walletId));
    }
}

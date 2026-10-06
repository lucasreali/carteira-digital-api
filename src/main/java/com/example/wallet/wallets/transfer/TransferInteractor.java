package com.example.wallet.wallets.transfer;

import com.example.wallet.wallets.domain.Money;
import com.example.wallet.wallets.domain.Transaction;
import com.example.wallet.wallets.domain.Wallet;
import com.example.wallet.wallets.domain.WalletRepository;
import java.time.Clock;
import java.time.Instant;

public class TransferInteractor implements TransferUseCase {

    private final WalletRepository wallets;
    private final Clock clock;

    public TransferInteractor(WalletRepository wallets, Clock clock) {
        this.wallets = wallets;
        this.clock = clock;
    }

    @Override
    public Transaction transfer(TransferCommand command) {
        Wallet source = wallets.findById(command.sourceWalletId()).orElseThrow();
        Wallet target = wallets.findById(command.targetWalletId()).orElseThrow();
        Transaction out = source.transferTo(target, Money.of(command.amount()), Instant.now(clock));
        wallets.save(source, target);
        return out;
    }
}

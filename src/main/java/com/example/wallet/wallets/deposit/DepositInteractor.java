package com.example.wallet.wallets.deposit;

import com.example.wallet.wallets.domain.Money;
import com.example.wallet.wallets.domain.Transaction;
import com.example.wallet.wallets.domain.WalletRepository;
import java.time.Clock;
import java.time.Instant;

public class DepositInteractor implements DepositUseCase {

    private final WalletRepository wallets;
    private final Clock clock;

    public DepositInteractor(WalletRepository wallets, Clock clock) {
        this.wallets = wallets;
        this.clock = clock;
    }

    @Override
    public Transaction deposit(DepositCommand command) {
        var wallet = wallets.findById(command.walletId()).orElseThrow();
        var transaction = wallet.deposit(Money.of(command.amount()), Instant.now(clock));
        wallets.save(wallet);
        return transaction;
    }
}

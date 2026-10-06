package com.example.wallet.wallets.withdraw;

import com.example.wallet.wallets.domain.Money;
import com.example.wallet.wallets.domain.Transaction;
import com.example.wallet.wallets.domain.WalletNotFoundException;
import com.example.wallet.wallets.domain.WalletRepository;
import java.time.Clock;
import java.time.Instant;

public class WithdrawInteractor implements WithdrawUseCase {

    private final WalletRepository wallets;
    private final Clock clock;

    public WithdrawInteractor(WalletRepository wallets, Clock clock) {
        this.wallets = wallets;
        this.clock = clock;
    }

    @Override
    public Transaction withdraw(WithdrawCommand command) {
        var wallet = wallets.findById(command.walletId()).orElseThrow(() -> new WalletNotFoundException(command.walletId()));
        wallet.ensureOwnedBy(command.requesterId());
        var transaction = wallet.withdraw(Money.of(command.amount()), Instant.now(clock));
        wallets.save(wallet);
        return transaction;
    }
}

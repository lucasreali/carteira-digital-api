package com.example.wallet.wallets.create;

import com.example.wallet.wallets.domain.Wallet;
import com.example.wallet.wallets.domain.WalletRepository;

public class CreateWalletInteractor implements CreateWalletUseCase {

    private final WalletRepository wallets;

    public CreateWalletInteractor(WalletRepository wallets) {
        this.wallets = wallets;
    }

    @Override
    public Wallet create(CreateWalletCommand command) {
        var wallet = Wallet.open(command.ownerId());
        wallets.save(wallet);
        return wallet;
    }
}

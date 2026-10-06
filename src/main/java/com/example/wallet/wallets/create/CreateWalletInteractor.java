package com.example.wallet.wallets.create;

import com.example.wallet.wallets.domain.Wallet;
import com.example.wallet.wallets.domain.WalletAlreadyExistsException;
import com.example.wallet.wallets.domain.WalletRepository;

public class CreateWalletInteractor implements CreateWalletUseCase {

    private final WalletRepository wallets;

    public CreateWalletInteractor(WalletRepository wallets) {
        this.wallets = wallets;
    }

    @Override
    public Wallet create(CreateWalletCommand command) {
        if (wallets.existsByOwnerId(command.ownerId())) {
            throw new WalletAlreadyExistsException();
        }
        var wallet = Wallet.open(command.ownerId());
        wallets.save(wallet);
        return wallet;
    }
}

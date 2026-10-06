package com.example.wallet.wallets.create;

import com.example.wallet.wallets.domain.Wallet;

public interface CreateWalletUseCase {

    Wallet create(CreateWalletCommand command);
}

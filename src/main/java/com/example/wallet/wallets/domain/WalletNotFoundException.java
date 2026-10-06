package com.example.wallet.wallets.domain;

import com.example.wallet.shared.domain.NotFoundException;
import java.util.UUID;

public class WalletNotFoundException extends NotFoundException {

    public WalletNotFoundException(UUID walletId) {
        super("Wallet " + walletId + " was not found.");
    }
}

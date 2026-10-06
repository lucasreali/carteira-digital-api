package com.example.wallet.wallets.domain;

import com.example.wallet.shared.domain.ConflictException;

public class WalletAlreadyExistsException extends ConflictException {

    public WalletAlreadyExistsException() {
        super("This user already has a wallet.");
    }
}

package com.example.wallet.wallets.domain;

import com.example.wallet.shared.domain.ForbiddenException;

public class WalletAccessDeniedException extends ForbiddenException {

    public WalletAccessDeniedException() {
        super("You do not have access to this wallet.");
    }
}

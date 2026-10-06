package com.example.wallet.wallets.domain;

import com.example.wallet.shared.domain.BusinessRuleException;

public class SameWalletTransferException extends BusinessRuleException {

    public SameWalletTransferException() {
        super("A wallet cannot transfer to itself.");
    }
}

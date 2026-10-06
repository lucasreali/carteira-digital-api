package com.example.wallet.wallets.domain;

import com.example.wallet.shared.domain.BusinessRuleException;

public class InsufficientFundsException extends BusinessRuleException {

    public InsufficientFundsException() {
        super("Insufficient funds: the wallet balance does not cover this amount.");
    }
}

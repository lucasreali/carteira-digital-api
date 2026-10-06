package com.example.wallet.wallets.domain;

import com.example.wallet.shared.domain.BusinessRuleException;

public class InvalidAmountException extends BusinessRuleException {

    public InvalidAmountException(String message) {
        super(message);
    }
}

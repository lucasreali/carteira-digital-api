package com.example.wallet.users.domain;

import com.example.wallet.shared.domain.BusinessRuleException;

public class InvalidEmailException extends BusinessRuleException {

    public InvalidEmailException() {
        super("Invalid email address");
    }
}

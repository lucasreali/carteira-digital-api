package com.example.wallet.users.domain;

import com.example.wallet.shared.domain.BusinessRuleException;

public class InvalidUserNameException extends BusinessRuleException {

    public InvalidUserNameException() {
        super("User name must not be blank");
    }
}

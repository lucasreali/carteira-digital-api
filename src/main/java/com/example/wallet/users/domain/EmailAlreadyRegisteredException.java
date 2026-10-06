package com.example.wallet.users.domain;

import com.example.wallet.shared.domain.ConflictException;

public class EmailAlreadyRegisteredException extends ConflictException {

    public EmailAlreadyRegisteredException() {
        super("Email already registered");
    }
}

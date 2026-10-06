package com.example.wallet.users.domain;

import com.example.wallet.shared.domain.UnauthorizedException;

/** Same message for unknown email and wrong password, so callers cannot probe which emails exist. */
public class InvalidCredentialsException extends UnauthorizedException {

    public InvalidCredentialsException() {
        super("Invalid email or password");
    }
}

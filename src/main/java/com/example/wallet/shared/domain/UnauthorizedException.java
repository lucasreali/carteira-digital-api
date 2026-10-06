package com.example.wallet.shared.domain;

/** The caller could not be authenticated (e.g. invalid credentials). */
public abstract non-sealed class UnauthorizedException extends DomainException {

    protected UnauthorizedException(String message) {
        super(message);
    }
}

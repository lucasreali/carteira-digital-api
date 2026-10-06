package com.example.wallet.shared.domain;

/** A requested resource does not exist. */
public abstract non-sealed class NotFoundException extends DomainException {

    protected NotFoundException(String message) {
        super(message);
    }
}

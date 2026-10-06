package com.example.wallet.shared.domain;

/** The request conflicts with the current state (duplicates, concurrent updates). */
public abstract non-sealed class ConflictException extends DomainException {

    protected ConflictException(String message) {
        super(message);
    }
}

package com.example.wallet.shared.domain;

/** The caller is authenticated but may not act on the resource. */
public abstract non-sealed class ForbiddenException extends DomainException {

    protected ForbiddenException(String message) {
        super(message);
    }
}

package com.example.wallet.shared.domain;

/** The request is well-formed but violates a business rule. */
public abstract non-sealed class BusinessRuleException extends DomainException {

    protected BusinessRuleException(String message) {
        super(message);
    }
}

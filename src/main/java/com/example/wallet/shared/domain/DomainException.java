package com.example.wallet.shared.domain;

/**
 * Root of all business errors. Modules never extend this directly: they extend one of the permitted
 * categories, which the web layer maps to an HTTP status.
 */
public abstract sealed class DomainException extends RuntimeException
        permits NotFoundException, ConflictException, BusinessRuleException, ForbiddenException, UnauthorizedException {

    protected DomainException(String message) {
        super(message);
    }
}

package com.example.wallet.wallets.domain;

import java.math.BigDecimal;

/** Non-negative monetary amount, always held with scale 2 so that 10 and 10.00 are equal. */
public record Money(BigDecimal value) {

    /** Matches the NUMERIC(19,2) column: 17 integer digits plus 2 decimals. */
    private static final int MAX_INTEGER_DIGITS = 17;

    private static final Money ZERO = new Money(BigDecimal.ZERO);

    public Money {
        if (value == null) {
            throw new InvalidAmountException("Amount is required.");
        }
        if (value.signum() < 0) {
            throw new InvalidAmountException("Amount must not be negative.");
        }
        if (value.stripTrailingZeros().scale() > 2) {
            throw new InvalidAmountException("Amount must have at most 2 decimal places.");
        }
        if (value.precision() - value.scale() > MAX_INTEGER_DIGITS) {
            throw new InvalidAmountException("Amount is too large.");
        }
        value = value.setScale(2);
    }

    public static Money of(BigDecimal value) {
        return new Money(value);
    }

    public static Money of(String value) {
        if (value == null) {
            throw new InvalidAmountException("Amount is required.");
        }
        try {
            return new Money(new BigDecimal(value));
        } catch (NumberFormatException e) {
            throw new InvalidAmountException("Amount must be a decimal number.");
        }
    }

    public static Money zero() {
        return ZERO;
    }

    public Money plus(Money other) {
        return new Money(value.add(other.value));
    }

    public Money minus(Money other) {
        return new Money(value.subtract(other.value));
    }

    public boolean isGreaterThan(Money other) {
        return value.compareTo(other.value) > 0;
    }

    public boolean isLessThan(Money other) {
        return value.compareTo(other.value) < 0;
    }

    /** Guard for operation amounts (deposit, withdrawal, transfer), which must be strictly positive. */
    public Money requirePositive() {
        if (value.signum() == 0) {
            throw new InvalidAmountException("Amount must be greater than zero.");
        }
        return this;
    }
}

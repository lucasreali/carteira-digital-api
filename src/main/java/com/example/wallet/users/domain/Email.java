package com.example.wallet.users.domain;

import java.util.Locale;
import java.util.regex.Pattern;

public record Email(String value) {

    // Pragmatic check (local@domain.tld, no whitespace), not full RFC 5322.
    private static final Pattern FORMAT = Pattern.compile("^[^@\\s]+@[^@\\s]+\\.[^@\\s]+$");

    public Email {
        if (value == null) {
            throw new InvalidEmailException();
        }
        value = value.trim().toLowerCase(Locale.ROOT);
        if (!FORMAT.matcher(value).matches()) {
            throw new InvalidEmailException();
        }
    }
}

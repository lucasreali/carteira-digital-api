package com.example.wallet.users.domain;

import java.util.Locale;

public record Email(String value) {

    public Email {
        value = value.trim().toLowerCase(Locale.ROOT);
    }
}

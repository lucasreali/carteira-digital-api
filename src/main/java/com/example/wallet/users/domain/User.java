package com.example.wallet.users.domain;

import java.util.UUID;

/** Registered user. The canonical constructor rehydrates from persistence; {@link #register} creates a new one. */
public record User(UUID id, String name, Email email, String passwordHash) {

    public User {
        name = name.trim();
    }

    public static User register(String name, Email email, String passwordHash) {
        return new User(UUID.randomUUID(), name, email, passwordHash);
    }
}

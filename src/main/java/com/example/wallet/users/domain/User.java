package com.example.wallet.users.domain;

import java.util.Objects;
import java.util.UUID;

/** Registered user. The canonical constructor rehydrates from persistence; {@link #register} creates a new one. */
public record User(UUID id, String name, Email email, String passwordHash) {

    public User {
        Objects.requireNonNull(id, "id");
        Objects.requireNonNull(email, "email");
        Objects.requireNonNull(passwordHash, "passwordHash");
        if (name == null || name.isBlank()) {
            throw new InvalidUserNameException();
        }
        name = name.trim();
    }

    public static User register(String name, Email email, String passwordHash) {
        return new User(UUID.randomUUID(), name, email, passwordHash);
    }

    @Override
    public String toString() {
        return "User[id=" + id + ", name=" + name + ", email=" + email.value() + "]";
    }
}

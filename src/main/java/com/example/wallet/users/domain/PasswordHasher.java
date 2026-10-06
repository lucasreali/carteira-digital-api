package com.example.wallet.users.domain;

public interface PasswordHasher {

    String hash(String raw);

    boolean matches(String raw, String hash);
}

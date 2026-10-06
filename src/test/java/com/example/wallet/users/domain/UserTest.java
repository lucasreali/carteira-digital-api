package com.example.wallet.users.domain;

import static org.assertj.core.api.Assertions.assertThat;

import org.junit.jupiter.api.Test;

class UserTest {

    private static final Email EMAIL = new Email("alice@example.com");
    private static final String HASH = "$2a$10$hash";

    @Test
    void registerAssignsFreshIdAndTrimmedName() {
        String name = "  Alice  ";

        User first = User.register(name, EMAIL, HASH);
        User second = User.register(name, EMAIL, HASH);

        assertThat(first.id()).isNotNull().isNotEqualTo(second.id());
        assertThat(first.name()).isEqualTo("Alice");
        assertThat(first.email()).isEqualTo(EMAIL);
        assertThat(first.passwordHash()).isEqualTo(HASH);
    }
}

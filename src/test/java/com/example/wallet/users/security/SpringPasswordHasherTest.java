package com.example.wallet.users.security;

import static org.assertj.core.api.Assertions.assertThat;

import org.junit.jupiter.api.Test;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;

class SpringPasswordHasherTest {

    private final SpringPasswordHasher hasher = new SpringPasswordHasher(new BCryptPasswordEncoder());

    @Test
    void hashesAndVerifiesPasswords() {
        String raw = "s3cret-password";

        String hash = hasher.hash(raw);

        assertThat(hash).isNotEqualTo(raw);
        assertThat(hasher.matches(raw, hash)).isTrue();
        assertThat(hasher.matches("wrong-password", hash)).isFalse();
    }
}

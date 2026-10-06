package com.example.wallet.users.domain;

import static org.assertj.core.api.Assertions.assertThat;

import org.junit.jupiter.api.Test;

class EmailTest {

    @Test
    void normalizesByTrimmingAndLowercasing() {
        String raw = "  Alice@Example.COM ";

        Email email = new Email(raw);

        assertThat(email.value()).isEqualTo("alice@example.com");
    }
}

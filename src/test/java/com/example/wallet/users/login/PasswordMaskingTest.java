package com.example.wallet.users.login;

import static org.assertj.core.api.Assertions.assertThat;

import org.junit.jupiter.api.Test;

class PasswordMaskingTest {

    @Test
    void loginCommandToStringMasksPassword() {
        String text = new LoginCommand("alice@example.com", "s3cret-password").toString();

        assertThat(text).contains("alice@example.com", "password=***").doesNotContain("s3cret-password");
    }

    @Test
    void loginRequestToStringMasksPassword() {
        String text = new LoginRequest("alice@example.com", "s3cret-password").toString();

        assertThat(text).contains("alice@example.com", "password=***").doesNotContain("s3cret-password");
    }
}

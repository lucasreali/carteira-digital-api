package com.example.wallet.users.register;

import static org.assertj.core.api.Assertions.assertThat;

import org.junit.jupiter.api.Test;

class PasswordMaskingTest {

    @Test
    void requestToStringMasksPassword() {
        var request = new RegisterUserRequest("Ana", "ana@example.com", "s3cret-pass");

        var text = request.toString();

        assertThat(text).doesNotContain("s3cret-pass").contains("password=***").contains("ana@example.com");
    }

    @Test
    void commandToStringMasksPassword() {
        var command = new RegisterUserCommand("Ana", "ana@example.com", "s3cret-pass");

        var text = command.toString();

        assertThat(text).doesNotContain("s3cret-pass").contains("password=***").contains("ana@example.com");
    }
}

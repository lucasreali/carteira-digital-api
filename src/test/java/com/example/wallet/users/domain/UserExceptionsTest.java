package com.example.wallet.users.domain;

import static org.assertj.core.api.Assertions.assertThat;

import com.example.wallet.shared.domain.ConflictException;
import com.example.wallet.shared.domain.UnauthorizedException;
import org.junit.jupiter.api.Test;

class UserExceptionsTest {

    @Test
    void duplicateEmailIsAConflict() {
        var exception = new EmailAlreadyRegisteredException();

        assertThat(exception).isInstanceOf(ConflictException.class).hasMessage("Email already registered");
    }

    @Test
    void invalidCredentialsIsUnauthorizedWithGenericMessage() {
        var exception = new InvalidCredentialsException();

        assertThat(exception).isInstanceOf(UnauthorizedException.class).hasMessage("Invalid email or password");
    }
}

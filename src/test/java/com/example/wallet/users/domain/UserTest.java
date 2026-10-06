package com.example.wallet.users.domain;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import com.example.wallet.shared.domain.BusinessRuleException;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.NullAndEmptySource;
import org.junit.jupiter.params.provider.ValueSource;

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

    @ParameterizedTest
    @NullAndEmptySource
    @ValueSource(strings = {"   "})
    void rejectsMissingName(String name) {
        assertThatThrownBy(() -> User.register(name, EMAIL, HASH))
                .isInstanceOf(InvalidUserNameException.class)
                .isInstanceOf(BusinessRuleException.class);
    }

    @Test
    void toStringOmitsPasswordHash() {
        User user = User.register("Alice", EMAIL, HASH);

        String text = user.toString();

        assertThat(text).contains(user.id().toString(), "Alice", EMAIL.value()).doesNotContain(HASH);
    }
}

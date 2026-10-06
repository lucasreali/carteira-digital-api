package com.example.wallet.users.domain;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import com.example.wallet.shared.domain.BusinessRuleException;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.NullAndEmptySource;
import org.junit.jupiter.params.provider.ValueSource;

class EmailTest {

    @Test
    void normalizesByTrimmingAndLowercasing() {
        String raw = "  Alice@Example.COM ";

        Email email = new Email(raw);

        assertThat(email.value()).isEqualTo("alice@example.com");
    }

    @ParameterizedTest
    @NullAndEmptySource
    @ValueSource(strings = {"   ", "alice", "alice@", "@example.com", "alice@example", "al ice@example.com", "a@b@c.com"})
    void rejectsMissingOrMalformedAddress(String raw) {
        assertThatThrownBy(() -> new Email(raw))
                .isInstanceOf(InvalidEmailException.class)
                .isInstanceOf(BusinessRuleException.class);
    }
}

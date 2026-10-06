package com.example.wallet.wallets.domain;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import com.example.wallet.shared.domain.BusinessRuleException;
import java.math.BigDecimal;
import org.junit.jupiter.api.Test;

class MoneyTest {

    @Test
    void normalizesToTwoDecimalPlaces() {
        var money = Money.of(new BigDecimal("10"));

        assertThat(money.value()).isEqualTo(new BigDecimal("10.00"));
    }

    @Test
    void equalityIgnoresRepresentation() {
        var plain = Money.of("10");
        var scaled = Money.of(new BigDecimal("10.00"));
        var trailingZeros = Money.of("10.000");

        assertThat(plain).isEqualTo(scaled).isEqualTo(trailingZeros);
        assertThat(plain).hasSameHashCodeAs(scaled);
    }

    @Test
    void zeroIsZero() {
        var zero = Money.zero();

        assertThat(zero).isEqualTo(Money.of("0.00"));
    }

    @Test
    void rejectsNegativeAmount() {
        assertThatThrownBy(() -> Money.of("-0.01"))
                .isInstanceOf(InvalidAmountException.class)
                .isInstanceOf(BusinessRuleException.class)
                .hasMessageContaining("negative");
    }

    @Test
    void rejectsMoreThanTwoDecimalPlaces() {
        assertThatThrownBy(() -> Money.of("10.001"))
                .isInstanceOf(InvalidAmountException.class)
                .hasMessageContaining("2 decimal places");
    }

    @Test
    void rejectsNullAmount() {
        assertThatThrownBy(() -> Money.of((BigDecimal) null)).isInstanceOf(InvalidAmountException.class);
        assertThatThrownBy(() -> Money.of((String) null)).isInstanceOf(InvalidAmountException.class);
    }

    @Test
    void rejectsAmountsThatDoNotFitNineteenDigits() {
        assertThat(Money.of("99999999999999999.99").value()).isEqualByComparingTo("99999999999999999.99");
        assertThatThrownBy(() -> Money.of("100000000000000000"))
                .isInstanceOf(InvalidAmountException.class)
                .hasMessageContaining("too large");
        assertThatThrownBy(() -> Money.of("1e1000000000")).isInstanceOf(InvalidAmountException.class);
    }

    @Test
    void rejectsMalformedText() {
        assertThatThrownBy(() -> Money.of("ten")).isInstanceOf(InvalidAmountException.class);
    }

    @Test
    void addsAndSubtracts() {
        var ten = Money.of("10.00");

        assertThat(ten.plus(Money.of("2.50"))).isEqualTo(Money.of("12.50"));
        assertThat(ten.minus(Money.of("2.50"))).isEqualTo(Money.of("7.50"));
    }

    @Test
    void subtractionBelowZeroIsRejected() {
        assertThatThrownBy(() -> Money.of("1.00").minus(Money.of("1.01")))
                .isInstanceOf(InvalidAmountException.class);
    }

    @Test
    void comparesByValue() {
        var ten = Money.of("10");

        assertThat(ten.isGreaterThan(Money.of("9.99"))).isTrue();
        assertThat(ten.isGreaterThan(Money.of("10.00"))).isFalse();
        assertThat(ten.isLessThan(Money.of("10.01"))).isTrue();
        assertThat(ten.isLessThan(Money.of("10.00"))).isFalse();
    }

    @Test
    void requirePositiveAcceptsPositiveAmount() {
        var amount = Money.of("0.01");

        assertThat(amount.requirePositive()).isSameAs(amount);
    }

    @Test
    void requirePositiveRejectsZero() {
        assertThatThrownBy(() -> Money.zero().requirePositive())
                .isInstanceOf(InvalidAmountException.class)
                .hasMessageContaining("greater than zero");
    }
}

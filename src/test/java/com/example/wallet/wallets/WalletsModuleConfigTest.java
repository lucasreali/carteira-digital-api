package com.example.wallet.wallets;

import static org.assertj.core.api.Assertions.assertThat;

import java.time.Instant;
import java.time.ZoneOffset;
import org.junit.jupiter.api.Test;

class WalletsModuleConfigTest {

    @Test
    void clockIsSystemUtc() {
        var clock = new WalletsModuleConfig().clock();

        assertThat(clock.getZone()).isEqualTo(ZoneOffset.UTC);
    }

    @Test
    void clockTicksInMicrosecondsToMatchDatabasePrecision() {
        var clock = new WalletsModuleConfig().clock();

        for (int i = 0; i < 100; i++) {
            assertThat(Instant.now(clock).getNano() % 1_000).isZero();
        }
    }
}

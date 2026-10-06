package com.example.wallet.wallets;

import static org.assertj.core.api.Assertions.assertThat;

import java.time.ZoneOffset;
import org.junit.jupiter.api.Test;

class WalletsModuleConfigTest {

    @Test
    void clockIsSystemUtc() {
        var clock = new WalletsModuleConfig().clock();

        assertThat(clock.getZone()).isEqualTo(ZoneOffset.UTC);
    }
}

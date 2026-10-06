package com.example.wallet.wallets.domain;

import static org.assertj.core.api.Assertions.assertThat;

import com.example.wallet.shared.domain.ConflictException;
import com.example.wallet.shared.domain.NotFoundException;
import java.util.UUID;
import org.junit.jupiter.api.Test;

class WalletExceptionsTest {

    @Test
    void walletNotFoundIsANotFoundNamingTheRequestedId() {
        var id = UUID.randomUUID();

        var exception = new WalletNotFoundException(id);

        assertThat(exception).isInstanceOf(NotFoundException.class).hasMessageContaining(id.toString());
    }

    @Test
    void walletAlreadyExistsIsAConflict() {
        var exception = new WalletAlreadyExistsException();

        assertThat(exception).isInstanceOf(ConflictException.class).hasMessageContaining("already has a wallet");
    }
}

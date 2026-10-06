package com.example.wallet.e2e;

import static org.hamcrest.Matchers.containsString;
import static org.hamcrest.Matchers.nullValue;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import org.junit.jupiter.api.Test;

class WalletJourneyE2ETest extends EndToEndTest {

    @Test
    void aliceDepositsWithdrawsAndTransfersToBob() throws Exception {
        var alice = newAccount("Alice");
        var bob = newAccount("Bob");

        deposit(alice.token(), alice.walletId(), "100.00").andExpect(status().isCreated());
        withdraw(alice.token(), alice.walletId(), "30.00").andExpect(status().isCreated());
        transfer(alice.token(), alice.walletId(), bob.walletId(), "20.00").andExpect(status().isCreated());

        balance(alice.token(), alice.walletId())
                .andExpect(status().isOk())
                .andExpect(content().string(containsString("\"balance\":50.00")));
        balance(bob.token(), bob.walletId())
                .andExpect(status().isOk())
                .andExpect(content().string(containsString("\"balance\":20.00")));
        statement(alice.token(), alice.walletId())
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(3))
                .andExpect(jsonPath("$[0].type").value("TRANSFER_OUT"))
                .andExpect(jsonPath("$[0].balanceAfter").value(50.00))
                .andExpect(jsonPath("$[0].counterpartyWalletId").value(bob.walletId()))
                .andExpect(jsonPath("$[1].type").value("WITHDRAWAL"))
                .andExpect(jsonPath("$[1].balanceAfter").value(70.00))
                .andExpect(jsonPath("$[2].type").value("DEPOSIT"))
                .andExpect(jsonPath("$[2].balanceAfter").value(100.00))
                .andExpect(jsonPath("$[2].counterpartyWalletId").value(nullValue()));
        statement(bob.token(), bob.walletId())
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(1))
                .andExpect(jsonPath("$[0].type").value("TRANSFER_IN"))
                .andExpect(jsonPath("$[0].amount").value(20.00))
                .andExpect(jsonPath("$[0].balanceAfter").value(20.00))
                .andExpect(jsonPath("$[0].counterpartyWalletId").value(alice.walletId()));
    }
}

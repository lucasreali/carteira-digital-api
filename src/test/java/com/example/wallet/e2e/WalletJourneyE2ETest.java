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
                .andExpect(jsonPath("$[0].counterpartyWalletId").value(bob.walletId()))
                .andExpect(jsonPath("$[1].type").value("WITHDRAWAL"))
                .andExpect(jsonPath("$[2].type").value("DEPOSIT"))
                .andExpect(jsonPath("$[2].counterpartyWalletId").value(nullValue()))
                .andExpect(content().string(containsString(entry("TRANSFER_OUT", "20.00", "50.00"))))
                .andExpect(content().string(containsString(entry("WITHDRAWAL", "30.00", "70.00"))))
                .andExpect(content().string(containsString(entry("DEPOSIT", "100.00", "100.00"))));
        statement(bob.token(), bob.walletId())
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(1))
                .andExpect(jsonPath("$[0].type").value("TRANSFER_IN"))
                .andExpect(jsonPath("$[0].counterpartyWalletId").value(alice.walletId()))
                .andExpect(content().string(containsString(entry("TRANSFER_IN", "20.00", "20.00"))));
    }

    /** Exact JSON text (money keeps two decimals); relies on TransactionResponse field order type, amount, balanceAfter. */
    private static String entry(String type, String amount, String balanceAfter) {
        return "\"type\":\"%s\",\"amount\":%s,\"balanceAfter\":%s".formatted(type, amount, balanceAfter);
    }
}

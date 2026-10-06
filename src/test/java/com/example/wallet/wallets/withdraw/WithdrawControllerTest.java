package com.example.wallet.wallets.withdraw;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.jwt;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.example.wallet.shared.security.SecurityConfig;
import com.example.wallet.wallets.domain.Money;
import com.example.wallet.wallets.domain.Transaction;
import com.example.wallet.wallets.domain.TransactionType;
import java.math.BigDecimal;
import java.time.Instant;
import java.util.UUID;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.ResultActions;

@WebMvcTest(WithdrawController.class)
@Import(SecurityConfig.class)
class WithdrawControllerTest {

    private final UUID userId = UUID.randomUUID();
    private final UUID walletId = UUID.randomUUID();

    @Autowired
    MockMvc mvc;

    @MockitoBean
    WithdrawUseCase useCase;

    private ResultActions postWithdrawal(String body) throws Exception {
        return mvc.perform(post("/wallets/{id}/withdrawals", walletId)
                .with(jwt().jwt(j -> j.subject(userId.toString())))
                .contentType(MediaType.APPLICATION_JSON)
                .content(body));
    }

    @Test
    void withdrawsAndReturnsCreatedTransaction() throws Exception {
        var transactionId = UUID.randomUUID();
        var occurredAt = Instant.parse("2026-10-05T12:00:00Z");
        when(useCase.withdraw(any())).thenReturn(new Transaction(
                transactionId, walletId, TransactionType.WITHDRAWAL,
                Money.of("30.00"), Money.of("70.00"), null, occurredAt));

        var result = postWithdrawal("{\"amount\":\"30.00\"}");

        result.andExpect(status().isCreated())
                .andExpect(jsonPath("$.id").value(transactionId.toString()))
                .andExpect(jsonPath("$.walletId").value(walletId.toString()))
                .andExpect(jsonPath("$.type").value("WITHDRAWAL"))
                .andExpect(jsonPath("$.amount").value(30.0))
                .andExpect(jsonPath("$.balanceAfter").value(70.0))
                .andExpect(jsonPath("$.counterpartyWalletId").isEmpty())
                .andExpect(jsonPath("$.occurredAt").value("2026-10-05T12:00:00Z"));
        verify(useCase).withdraw(new WithdrawCommand(walletId, userId, new BigDecimal("30.00")));
    }
}

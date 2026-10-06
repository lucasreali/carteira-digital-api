package com.example.wallet.wallets.statement;

import static org.hamcrest.Matchers.containsString;
import static org.hamcrest.Matchers.nullValue;
import static org.mockito.BDDMockito.given;
import static org.mockito.BDDMockito.then;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.jwt;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.example.wallet.shared.security.SecurityConfig;
import com.example.wallet.wallets.domain.Money;
import com.example.wallet.wallets.domain.Transaction;
import com.example.wallet.wallets.domain.TransactionType;
import com.example.wallet.wallets.domain.WalletAccessDeniedException;
import com.example.wallet.wallets.domain.WalletNotFoundException;
import java.time.Instant;
import java.util.List;
import java.util.UUID;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

@WebMvcTest(GetStatementController.class)
@Import(SecurityConfig.class)
class GetStatementControllerTest {

    @Autowired
    MockMvc mvc;

    @MockitoBean
    GetStatementUseCase useCase;

    private final UUID userId = UUID.randomUUID();
    private final UUID walletId = UUID.randomUUID();

    @Test
    void returnsTransactionsNewestFirst() throws Exception {
        var counterparty = UUID.randomUUID();
        var transferIn = new Transaction(UUID.randomUUID(), walletId, TransactionType.TRANSFER_IN,
                Money.of("2.50"), Money.of("12.50"), counterparty, Instant.parse("2026-01-02T10:00:00Z"));
        var deposit = new Transaction(UUID.randomUUID(), walletId, TransactionType.DEPOSIT,
                Money.of("10.00"), Money.of("10.00"), null, Instant.parse("2026-01-01T10:00:00Z"));
        given(useCase.getStatement(new GetStatementQuery(walletId, userId))).willReturn(List.of(transferIn, deposit));

        var result = mvc.perform(get("/wallets/{id}/transactions", walletId)
                .with(jwt().jwt(j -> j.subject(userId.toString()))));

        result.andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(2))
                .andExpect(jsonPath("$[0].id").value(transferIn.id().toString()))
                .andExpect(jsonPath("$[0].walletId").value(walletId.toString()))
                .andExpect(jsonPath("$[0].type").value("TRANSFER_IN"))
                .andExpect(jsonPath("$[0].counterpartyWalletId").value(counterparty.toString()))
                .andExpect(jsonPath("$[0].occurredAt").value("2026-01-02T10:00:00Z"))
                .andExpect(jsonPath("$[1].id").value(deposit.id().toString()))
                .andExpect(jsonPath("$[1].type").value("DEPOSIT"))
                .andExpect(jsonPath("$[1].counterpartyWalletId").value(nullValue()))
                .andExpect(content().string(containsString("\"amount\":2.50,\"balanceAfter\":12.50")))
                .andExpect(content().string(containsString("\"amount\":10.00,\"balanceAfter\":10.00")));
    }

    @Test
    void returnsEmptyArrayWhenWalletHasNoTransactions() throws Exception {
        given(useCase.getStatement(new GetStatementQuery(walletId, userId))).willReturn(List.of());

        var result = mvc.perform(get("/wallets/{id}/transactions", walletId)
                .with(jwt().jwt(j -> j.subject(userId.toString()))));

        result.andExpect(status().isOk())
                .andExpect(content().json("[]"));
    }

    @Test
    void rejectsRequestWithoutToken() throws Exception {
        var result = mvc.perform(get("/wallets/{id}/transactions", walletId));

        result.andExpect(status().isUnauthorized());
        then(useCase).shouldHaveNoInteractions();
    }

    @Test
    void returnsNotFoundForUnknownWallet() throws Exception {
        given(useCase.getStatement(new GetStatementQuery(walletId, userId)))
                .willThrow(new WalletNotFoundException(walletId));

        var result = mvc.perform(get("/wallets/{id}/transactions", walletId)
                .with(jwt().jwt(j -> j.subject(userId.toString()))));

        result.andExpect(status().isNotFound())
                .andExpect(content().contentTypeCompatibleWith(MediaType.APPLICATION_PROBLEM_JSON))
                .andExpect(jsonPath("$.detail").value("Wallet " + walletId + " was not found."));
    }

    @Test
    void returnsForbiddenForWalletOfAnotherUser() throws Exception {
        given(useCase.getStatement(new GetStatementQuery(walletId, userId)))
                .willThrow(new WalletAccessDeniedException());

        var result = mvc.perform(get("/wallets/{id}/transactions", walletId)
                .with(jwt().jwt(j -> j.subject(userId.toString()))));

        result.andExpect(status().isForbidden())
                .andExpect(content().contentTypeCompatibleWith(MediaType.APPLICATION_PROBLEM_JSON))
                .andExpect(jsonPath("$.detail").value("You do not have access to this wallet."));
    }
}

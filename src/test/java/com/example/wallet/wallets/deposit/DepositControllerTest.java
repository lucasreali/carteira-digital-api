package com.example.wallet.wallets.deposit;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.jwt;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.example.wallet.shared.security.SecurityConfig;
import com.example.wallet.wallets.domain.InvalidAmountException;
import com.example.wallet.wallets.domain.Money;
import com.example.wallet.wallets.domain.Transaction;
import com.example.wallet.wallets.domain.TransactionType;
import com.example.wallet.wallets.domain.WalletAccessDeniedException;
import com.example.wallet.wallets.domain.WalletNotFoundException;
import java.math.BigDecimal;
import java.time.Instant;
import java.util.UUID;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.context.annotation.Import;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.request.MockHttpServletRequestBuilder;

@WebMvcTest(DepositController.class)
@Import(SecurityConfig.class)
class DepositControllerTest {

    private final UUID walletId = UUID.randomUUID();
    private final UUID userId = UUID.randomUUID();

    @Autowired
    MockMvc mvc;

    @MockitoBean
    DepositUseCase useCase;

    private MockHttpServletRequestBuilder deposit(String body) {
        return post("/wallets/{id}/deposits", walletId)
                .with(jwt().jwt(j -> j.subject(userId.toString())))
                .contentType(MediaType.APPLICATION_JSON)
                .content(body);
    }

    @Test
    void returnsCreatedDepositTransaction() throws Exception {
        var transaction = new Transaction(UUID.randomUUID(), walletId, TransactionType.DEPOSIT,
                Money.of("10.50"), Money.of("15.50"), null, Instant.parse("2026-10-05T12:00:00Z"));
        given(useCase.deposit(any())).willReturn(transaction);

        var result = mvc.perform(deposit("{\"amount\":10.50}"));

        result.andExpect(status().isCreated())
                .andExpect(jsonPath("$.id").value(transaction.id().toString()))
                .andExpect(jsonPath("$.walletId").value(walletId.toString()))
                .andExpect(jsonPath("$.type").value("DEPOSIT"))
                .andExpect(jsonPath("$.amount").value(10.50))
                .andExpect(jsonPath("$.balanceAfter").value(15.50))
                .andExpect(jsonPath("$.occurredAt").value("2026-10-05T12:00:00Z"));
        verify(useCase).deposit(new DepositCommand(walletId, userId, new BigDecimal("10.50")));
    }

    @Test
    void rejectsRequestWithoutToken() throws Exception {
        var request = post("/wallets/{id}/deposits", walletId)
                .contentType(MediaType.APPLICATION_JSON)
                .content("{\"amount\":10.00}");

        var result = mvc.perform(request);

        result.andExpect(status().isUnauthorized());
        verifyNoInteractions(useCase);
    }

    @ParameterizedTest
    @ValueSource(strings = {"{}", "{\"amount\":null}", "{\"amount\":-1.00}", "{\"amount\":0}"})
    void rejectsMissingOrNonPositiveAmount(String body) throws Exception {
        var request = deposit(body);

        var result = mvc.perform(request);

        result.andExpect(status().isBadRequest())
                .andExpect(content().contentTypeCompatibleWith(MediaType.APPLICATION_PROBLEM_JSON))
                .andExpect(jsonPath("$.errors.amount").exists());
        verifyNoInteractions(useCase);
    }

    @Test
    void mapsUnknownWalletToNotFound() throws Exception {
        given(useCase.deposit(any())).willThrow(new WalletNotFoundException(walletId));

        var result = mvc.perform(deposit("{\"amount\":10.00}"));

        result.andExpect(status().isNotFound())
                .andExpect(content().contentTypeCompatibleWith(MediaType.APPLICATION_PROBLEM_JSON))
                .andExpect(jsonPath("$.detail").value("Wallet " + walletId + " was not found."));
    }

    @Test
    void mapsWalletOfAnotherOwnerToForbidden() throws Exception {
        given(useCase.deposit(any())).willThrow(new WalletAccessDeniedException());

        var result = mvc.perform(deposit("{\"amount\":10.00}"));

        result.andExpect(status().isForbidden())
                .andExpect(content().contentTypeCompatibleWith(MediaType.APPLICATION_PROBLEM_JSON));
    }

    @Test
    void mapsInvalidAmountToUnprocessableContent() throws Exception {
        given(useCase.deposit(any())).willThrow(new InvalidAmountException("Amount must have at most 2 decimal places."));

        var result = mvc.perform(deposit("{\"amount\":10.001}"));

        result.andExpect(status().is(HttpStatus.UNPROCESSABLE_CONTENT.value()))
                .andExpect(content().contentTypeCompatibleWith(MediaType.APPLICATION_PROBLEM_JSON))
                .andExpect(jsonPath("$.detail").value("Amount must have at most 2 decimal places."));
    }
}

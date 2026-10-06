package com.example.wallet.wallets.withdraw;

import static org.hamcrest.Matchers.containsString;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.jwt;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.example.wallet.shared.security.SecurityConfig;
import com.example.wallet.wallets.domain.InsufficientFundsException;
import com.example.wallet.wallets.domain.Money;
import com.example.wallet.wallets.domain.Transaction;
import com.example.wallet.wallets.domain.TransactionType;
import com.example.wallet.wallets.domain.WalletAccessDeniedException;
import com.example.wallet.wallets.domain.WalletNotFoundException;
import java.math.BigDecimal;
import java.time.Instant;
import java.util.UUID;
import java.util.stream.Stream;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;
import org.junit.jupiter.params.provider.ValueSource;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.context.annotation.Import;
import org.springframework.http.HttpStatus;
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
                .andExpect(content().string(containsString("\"amount\":30.00")))
                .andExpect(content().string(containsString("\"balanceAfter\":70.00")))
                .andExpect(jsonPath("$.counterpartyWalletId").isEmpty())
                .andExpect(jsonPath("$.occurredAt").value("2026-10-05T12:00:00Z"));
        verify(useCase).withdraw(new WithdrawCommand(walletId, userId, new BigDecimal("30.00")));
    }

    @ParameterizedTest
    @ValueSource(strings = {"{}", "{\"amount\":null}", "{\"amount\":\"-5.00\"}", "{\"amount\":0}"})
    void rejectsMissingOrNonPositiveAmount(String body) throws Exception {
        var result = postWithdrawal(body);

        result.andExpect(status().isBadRequest())
                .andExpect(content().contentTypeCompatibleWith(MediaType.APPLICATION_PROBLEM_JSON))
                .andExpect(jsonPath("$.errors.amount").isNotEmpty());
        verifyNoInteractions(useCase);
    }

    @Test
    void rejectsRequestWithoutToken() throws Exception {
        var result = mvc.perform(post("/wallets/{id}/withdrawals", walletId)
                .contentType(MediaType.APPLICATION_JSON)
                .content("{\"amount\":\"10.00\"}"));

        result.andExpect(status().isUnauthorized());
        verifyNoInteractions(useCase);
    }

    static Stream<Arguments> domainErrors() {
        return Stream.of(
                Arguments.of(new WalletNotFoundException(UUID.randomUUID()), HttpStatus.NOT_FOUND),
                Arguments.of(new WalletAccessDeniedException(), HttpStatus.FORBIDDEN),
                Arguments.of(new InsufficientFundsException(), HttpStatus.UNPROCESSABLE_CONTENT));
    }

    @ParameterizedTest
    @MethodSource("domainErrors")
    void mapsDomainErrorsToProblemDetail(RuntimeException error, HttpStatus expectedStatus) throws Exception {
        when(useCase.withdraw(any())).thenThrow(error);

        var result = postWithdrawal("{\"amount\":\"10.00\"}");

        result.andExpect(status().is(expectedStatus.value()))
                .andExpect(content().contentTypeCompatibleWith(MediaType.APPLICATION_PROBLEM_JSON))
                .andExpect(jsonPath("$.detail").value(error.getMessage()));
    }
}

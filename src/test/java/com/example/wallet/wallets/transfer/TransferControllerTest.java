package com.example.wallet.wallets.transfer;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.jwt;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.example.wallet.shared.security.SecurityConfig;
import com.example.wallet.wallets.domain.Money;
import com.example.wallet.wallets.domain.SameWalletTransferException;
import com.example.wallet.wallets.domain.Transaction;
import com.example.wallet.wallets.domain.TransactionType;
import com.example.wallet.wallets.domain.WalletAccessDeniedException;
import com.example.wallet.wallets.domain.WalletNotFoundException;
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

@WebMvcTest(TransferController.class)
@Import(SecurityConfig.class)
class TransferControllerTest {

    @Autowired
    MockMvc mvc;

    @MockitoBean
    TransferUseCase useCase;

    private final UUID userId = UUID.randomUUID();
    private final UUID sourceId = UUID.randomUUID();
    private final UUID targetId = UUID.randomUUID();

    private ResultActions postTransfer(String body) throws Exception {
        return mvc.perform(post("/wallets/{id}/transfers", sourceId)
                .with(jwt().jwt(j -> j.subject(userId.toString())))
                .contentType(MediaType.APPLICATION_JSON)
                .content(body));
    }

    private String validBody() {
        return "{\"targetWalletId\":\"" + targetId + "\",\"amount\":\"30.00\"}";
    }

    @Test
    void returnsTheTransferOutTransactionWith201() throws Exception {
        UUID txId = UUID.randomUUID();
        given(useCase.transfer(new TransferCommand(sourceId, userId, targetId, new BigDecimal("30.00"))))
                .willReturn(new Transaction(txId, sourceId, TransactionType.TRANSFER_OUT, Money.of("30.00"),
                        Money.of("70.00"), targetId, Instant.parse("2026-10-05T12:00:00Z")));

        var result = postTransfer(validBody());

        result.andExpect(status().isCreated())
                .andExpect(jsonPath("$.id").value(txId.toString()))
                .andExpect(jsonPath("$.walletId").value(sourceId.toString()))
                .andExpect(jsonPath("$.type").value("TRANSFER_OUT"))
                .andExpect(jsonPath("$.amount").value(30.00))
                .andExpect(jsonPath("$.balanceAfter").value(70.00))
                .andExpect(jsonPath("$.counterpartyWalletId").value(targetId.toString()))
                .andExpect(jsonPath("$.occurredAt").value("2026-10-05T12:00:00Z"));
    }

    @Test
    void requiresAuthentication() throws Exception {
        var result = mvc.perform(post("/wallets/{id}/transfers", sourceId)
                .contentType(MediaType.APPLICATION_JSON)
                .content(validBody()));

        result.andExpect(status().isUnauthorized());
        verifyNoInteractions(useCase);
    }

    @Test
    void rejectsMissingTargetAndAmount() throws Exception {
        var result = postTransfer("{}");

        result.andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.errors.targetWalletId").exists())
                .andExpect(jsonPath("$.errors.amount").exists());
        verifyNoInteractions(useCase);
    }

    @Test
    void rejectsNegativeAmount() throws Exception {
        var result = postTransfer("{\"targetWalletId\":\"" + targetId + "\",\"amount\":\"-5.00\"}");

        result.andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.errors.amount").exists());
        verifyNoInteractions(useCase);
    }

    @Test
    void answersUnknownWalletWith404ProblemDetail() throws Exception {
        given(useCase.transfer(any())).willThrow(new WalletNotFoundException(targetId));

        var result = postTransfer(validBody());

        result.andExpect(status().isNotFound())
                .andExpect(content().contentTypeCompatibleWith(MediaType.APPLICATION_PROBLEM_JSON))
                .andExpect(jsonPath("$.detail").value(new WalletNotFoundException(targetId).getMessage()));
    }

    @Test
    void answersForeignSourceWalletWith403ProblemDetail() throws Exception {
        given(useCase.transfer(any())).willThrow(new WalletAccessDeniedException());

        var result = postTransfer(validBody());

        result.andExpect(status().isForbidden())
                .andExpect(content().contentTypeCompatibleWith(MediaType.APPLICATION_PROBLEM_JSON))
                .andExpect(jsonPath("$.detail").value(new WalletAccessDeniedException().getMessage()));
    }

    @Test
    void answersBusinessRuleViolationWith422ProblemDetail() throws Exception {
        given(useCase.transfer(any())).willThrow(new SameWalletTransferException());

        var result = postTransfer(validBody());

        result.andExpect(status().isUnprocessableContent())
                .andExpect(content().contentTypeCompatibleWith(MediaType.APPLICATION_PROBLEM_JSON))
                .andExpect(jsonPath("$.detail").value(new SameWalletTransferException().getMessage()));
    }
}

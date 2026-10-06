package com.example.wallet.wallets.balance;

import static org.hamcrest.Matchers.containsString;
import static org.mockito.BDDMockito.given;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.jwt;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.example.wallet.shared.security.SecurityConfig;
import com.example.wallet.wallets.domain.Money;
import java.util.UUID;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.context.annotation.Import;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

@WebMvcTest(GetBalanceController.class)
@Import(SecurityConfig.class)
class GetBalanceControllerTest {

    @Autowired
    MockMvc mvc;

    @MockitoBean
    GetBalanceUseCase useCase;

    private final UUID userId = UUID.randomUUID();
    private final UUID walletId = UUID.randomUUID();

    @Test
    void returnsBalanceOfOwnersWallet() throws Exception {
        given(useCase.getBalance(new GetBalanceQuery(walletId, userId))).willReturn(Money.zero());

        var result = mvc.perform(get("/wallets/{id}/balance", walletId)
                .with(jwt().jwt(j -> j.subject(userId.toString()))));

        result.andExpect(status().isOk())
                .andExpect(jsonPath("$.walletId").value(walletId.toString()))
                .andExpect(content().string(containsString("\"balance\":0.00")));
    }
}

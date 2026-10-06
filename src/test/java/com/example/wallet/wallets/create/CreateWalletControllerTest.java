package com.example.wallet.wallets.create;

import static org.hamcrest.Matchers.containsString;
import static org.mockito.BDDMockito.given;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.jwt;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.header;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.example.wallet.shared.security.SecurityConfig;
import com.example.wallet.wallets.domain.Wallet;
import java.util.UUID;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.context.annotation.Import;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

@WebMvcTest(CreateWalletController.class)
@Import(SecurityConfig.class)
class CreateWalletControllerTest {

    @Autowired
    MockMvc mvc;

    @MockitoBean
    CreateWalletUseCase useCase;

    private final UUID userId = UUID.randomUUID();

    @Test
    void createsWalletForAuthenticatedUser() throws Exception {
        var wallet = Wallet.open(userId);
        given(useCase.create(new CreateWalletCommand(userId))).willReturn(wallet);

        var result = mvc.perform(post("/wallets").with(jwt().jwt(j -> j.subject(userId.toString()))));

        result.andExpect(status().isCreated())
                .andExpect(header().string("Location", "/wallets/" + wallet.id()))
                .andExpect(jsonPath("$.id").value(wallet.id().toString()))
                .andExpect(jsonPath("$.ownerId").value(userId.toString()))
                .andExpect(content().string(containsString("\"balance\":0.00")));
    }
}

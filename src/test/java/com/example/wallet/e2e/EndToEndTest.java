package com.example.wallet.e2e;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.jayway.jsonpath.JsonPath;
import java.util.UUID;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.ResultActions;

/**
 * Full application (HTTP + security + JPA/H2), no mocks. The H2 database is shared by every test in the
 * context, so each test registers its own users with unique emails.
 */
@SpringBootTest
@AutoConfigureMockMvc
abstract class EndToEndTest {

    static final String PASSWORD = "s3cret-pass";

    @Autowired
    MockMvc mvc;

    /** A registered user with a valid access token and one wallet. */
    record Account(String email, String token, String walletId) {}

    Account newAccount(String name) throws Exception {
        String email = uniqueEmail(name);
        register(name, email).andExpect(status().isCreated());
        String token = login(email, PASSWORD);
        String walletId = JsonPath.read(createWallet(token)
                .andExpect(status().isCreated())
                .andReturn().getResponse().getContentAsString(), "$.id");
        return new Account(email, token, walletId);
    }

    static String uniqueEmail(String name) {
        return name.toLowerCase() + "-" + UUID.randomUUID() + "@example.com";
    }

    ResultActions register(String name, String email) throws Exception {
        return mvc.perform(post("/users").contentType(MediaType.APPLICATION_JSON).content("""
                {"name":"%s","email":"%s","password":"%s"}""".formatted(name, email, PASSWORD)));
    }

    ResultActions attemptLogin(String email, String password) throws Exception {
        return mvc.perform(post("/auth/login").contentType(MediaType.APPLICATION_JSON).content("""
                {"email":"%s","password":"%s"}""".formatted(email, password)));
    }

    String login(String email, String password) throws Exception {
        String body = attemptLogin(email, password)
                .andExpect(status().isOk())
                .andReturn().getResponse().getContentAsString();
        return JsonPath.read(body, "$.accessToken");
    }

    ResultActions createWallet(String token) throws Exception {
        return mvc.perform(post("/wallets").header(HttpHeaders.AUTHORIZATION, bearer(token)));
    }

    ResultActions deposit(String token, String walletId, String amount) throws Exception {
        return postAmount(token, "/wallets/{id}/deposits", walletId, "{\"amount\":%s}".formatted(amount));
    }

    ResultActions withdraw(String token, String walletId, String amount) throws Exception {
        return postAmount(token, "/wallets/{id}/withdrawals", walletId, "{\"amount\":%s}".formatted(amount));
    }

    ResultActions transfer(String token, String sourceWalletId, String targetWalletId, String amount) throws Exception {
        return postAmount(token, "/wallets/{id}/transfers", sourceWalletId,
                "{\"targetWalletId\":\"%s\",\"amount\":%s}".formatted(targetWalletId, amount));
    }

    ResultActions balance(String token, String walletId) throws Exception {
        return mvc.perform(get("/wallets/{id}/balance", walletId).header(HttpHeaders.AUTHORIZATION, bearer(token)));
    }

    ResultActions statement(String token, String walletId) throws Exception {
        return mvc.perform(get("/wallets/{id}/transactions", walletId).header(HttpHeaders.AUTHORIZATION, bearer(token)));
    }

    private ResultActions postAmount(String token, String path, String walletId, String json) throws Exception {
        return mvc.perform(post(path, walletId)
                .header(HttpHeaders.AUTHORIZATION, bearer(token))
                .contentType(MediaType.APPLICATION_JSON)
                .content(json));
    }

    private static String bearer(String token) {
        return "Bearer " + token;
    }
}

package com.example.wallet.e2e;

import static org.hamcrest.Matchers.containsString;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import java.util.UUID;
import org.junit.jupiter.api.Test;
import org.springframework.http.MediaType;

class ErrorContractE2ETest extends EndToEndTest {

    private static final String INVALID_CREDENTIALS = "Invalid email or password";

    @Test
    void registeringAnEmailTwiceIsAConflict() throws Exception {
        String email = uniqueEmail("dup");
        register("First", email).andExpect(status().isCreated());

        var result = register("Second", email);

        result.andExpect(status().isConflict())
                .andExpect(content().contentTypeCompatibleWith(MediaType.APPLICATION_PROBLEM_JSON));
    }

    @Test
    void wrongPasswordAndUnknownEmailAreIndistinguishable() throws Exception {
        String email = uniqueEmail("carol");
        register("Carol", email).andExpect(status().isCreated());

        var wrongPassword = attemptLogin(email, "not-the-password");
        var unknownEmail = attemptLogin(uniqueEmail("ghost"), PASSWORD);

        wrongPassword.andExpect(status().isUnauthorized())
                .andExpect(content().contentTypeCompatibleWith(MediaType.APPLICATION_PROBLEM_JSON))
                .andExpect(jsonPath("$.detail").value(INVALID_CREDENTIALS));
        unknownEmail.andExpect(status().isUnauthorized())
                .andExpect(content().contentTypeCompatibleWith(MediaType.APPLICATION_PROBLEM_JSON))
                .andExpect(jsonPath("$.detail").value(INVALID_CREDENTIALS));
    }

    @Test
    void protectedRouteWithoutTokenIsUnauthorized() throws Exception {
        var alice = newAccount("Alice");

        var result = mvc.perform(get("/wallets/{id}/balance", alice.walletId()));

        result.andExpect(status().isUnauthorized());
    }

    @Test
    void tamperedOrGarbageTokenIsUnauthorized() throws Exception {
        var alice = newAccount("Alice");
        var bob = newAccount("Bob");
        // Alice's header and claims signed with Bob's signature: well-formed, but the signature does not match.
        String tampered = alice.token().substring(0, alice.token().lastIndexOf('.'))
                + bob.token().substring(bob.token().lastIndexOf('.'));

        var tamperedResult = balance(tampered, alice.walletId());
        var garbageResult = balance("not-a-jwt", alice.walletId());

        tamperedResult.andExpect(status().isUnauthorized());
        garbageResult.andExpect(status().isUnauthorized());
    }

    @Test
    void secondWalletForSameUserIsAConflict() throws Exception {
        var alice = newAccount("Alice");

        var result = createWallet(alice.token());

        result.andExpect(status().isConflict())
                .andExpect(content().contentTypeCompatibleWith(MediaType.APPLICATION_PROBLEM_JSON));
    }

    @Test
    void anotherUserCannotTouchSomeoneElsesWallet() throws Exception {
        var alice = newAccount("Alice");
        var bob = newAccount("Bob");

        var readBalance = balance(bob.token(), alice.walletId());
        var readStatement = statement(bob.token(), alice.walletId());
        var depositInto = deposit(bob.token(), alice.walletId(), "10.00");

        readBalance.andExpect(status().isForbidden())
                .andExpect(content().contentTypeCompatibleWith(MediaType.APPLICATION_PROBLEM_JSON));
        readStatement.andExpect(status().isForbidden());
        depositInto.andExpect(status().isForbidden());
        balance(alice.token(), alice.walletId()).andExpect(content().string(containsString("\"balance\":0.00")));
    }

    @Test
    void unknownWalletIsNotFound() throws Exception {
        var alice = newAccount("Alice");

        var result = balance(alice.token(), UUID.randomUUID().toString());

        result.andExpect(status().isNotFound())
                .andExpect(content().contentTypeCompatibleWith(MediaType.APPLICATION_PROBLEM_JSON));
    }

    @Test
    void malformedWalletIdIsBadRequest() throws Exception {
        var alice = newAccount("Alice");

        var result = balance(alice.token(), "not-a-uuid");

        result.andExpect(status().isBadRequest())
                .andExpect(content().contentTypeCompatibleWith(MediaType.APPLICATION_PROBLEM_JSON));
    }

    @Test
    void withdrawingMoreThanTheBalanceIsRejectedAndKeepsTheBalance() throws Exception {
        var alice = newAccount("Alice");
        deposit(alice.token(), alice.walletId(), "10.00").andExpect(status().isCreated());

        var result = withdraw(alice.token(), alice.walletId(), "10.01");

        result.andExpect(status().isUnprocessableContent())
                .andExpect(content().contentTypeCompatibleWith(MediaType.APPLICATION_PROBLEM_JSON));
        balance(alice.token(), alice.walletId()).andExpect(content().string(containsString("\"balance\":10.00")));
    }

    @Test
    void transferToTheSameWalletIsRejected() throws Exception {
        var alice = newAccount("Alice");
        deposit(alice.token(), alice.walletId(), "10.00").andExpect(status().isCreated());

        var result = transfer(alice.token(), alice.walletId(), alice.walletId(), "1.00");

        result.andExpect(status().isUnprocessableContent())
                .andExpect(content().contentTypeCompatibleWith(MediaType.APPLICATION_PROBLEM_JSON));
    }

    @Test
    void amountWithThreeDecimalsIsRejected() throws Exception {
        var alice = newAccount("Alice");

        var result = deposit(alice.token(), alice.walletId(), "1.005");

        result.andExpect(status().isUnprocessableContent())
                .andExpect(content().contentTypeCompatibleWith(MediaType.APPLICATION_PROBLEM_JSON));
    }

    @Test
    void negativeAmountIsAValidationError() throws Exception {
        var alice = newAccount("Alice");

        var result = deposit(alice.token(), alice.walletId(), "-5.00");

        result.andExpect(status().isBadRequest())
                .andExpect(content().contentTypeCompatibleWith(MediaType.APPLICATION_PROBLEM_JSON))
                .andExpect(jsonPath("$.errors.amount").exists());
    }

    @Test
    void transferWithInsufficientFundsLeavesBothBalancesUnchanged() throws Exception {
        var alice = newAccount("Alice");
        var bob = newAccount("Bob");
        deposit(alice.token(), alice.walletId(), "15.00").andExpect(status().isCreated());
        deposit(bob.token(), bob.walletId(), "5.00").andExpect(status().isCreated());

        var result = transfer(alice.token(), alice.walletId(), bob.walletId(), "15.01");

        result.andExpect(status().isUnprocessableContent())
                .andExpect(content().contentTypeCompatibleWith(MediaType.APPLICATION_PROBLEM_JSON));
        balance(alice.token(), alice.walletId()).andExpect(content().string(containsString("\"balance\":15.00")));
        balance(bob.token(), bob.walletId()).andExpect(content().string(containsString("\"balance\":5.00")));
        statement(bob.token(), bob.walletId()).andExpect(jsonPath("$.length()").value(1));
    }
}

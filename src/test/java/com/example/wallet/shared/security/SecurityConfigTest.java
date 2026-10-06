package com.example.wallet.shared.security;

import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.jwt;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.context.annotation.Import;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RestController;

@WebMvcTest(SecurityConfigTest.ProbeController.class)
@Import({SecurityConfig.class, SecurityConfigTest.ProbeController.class})
class SecurityConfigTest {

    @RestController
    static class ProbeController {

        @GetMapping("/protected")
        String whoAmI(@AuthenticationPrincipal Jwt jwt) {
            return jwt.getSubject();
        }

        @PostMapping("/users")
        String register() {
            return "registered";
        }

        @PostMapping("/auth/login")
        String login() {
            return "logged-in";
        }
    }

    @Autowired
    MockMvc mvc;

    @Test
    void rejectsUnauthenticatedRequestToProtectedEndpoint() throws Exception {
        var request = get("/protected");

        var result = mvc.perform(request);

        result.andExpect(status().isUnauthorized());
    }

    @Test
    void acceptsJwtAuthenticatedRequestAndExposesSubject() throws Exception {
        var request = get("/protected").with(jwt().jwt(j -> j.subject("user-42")));

        var result = mvc.perform(request);

        result.andExpect(status().isOk()).andExpect(content().string("user-42"));
    }

    @Test
    void allowsRegistrationWithoutToken() throws Exception {
        var request = post("/users");

        var result = mvc.perform(request);

        result.andExpect(status().isOk());
    }

    @Test
    void allowsLoginWithoutToken() throws Exception {
        var request = post("/auth/login");

        var result = mvc.perform(request);

        result.andExpect(status().isOk());
    }
}

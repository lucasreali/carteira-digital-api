package com.example.wallet.shared.web;

import static org.hamcrest.Matchers.containsString;
import static org.hamcrest.Matchers.not;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.jwt;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.example.wallet.shared.domain.BusinessRuleException;
import com.example.wallet.shared.domain.ConflictException;
import com.example.wallet.shared.domain.ForbiddenException;
import com.example.wallet.shared.domain.NotFoundException;
import com.example.wallet.shared.domain.UnauthorizedException;
import com.example.wallet.shared.security.SecurityConfig;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.context.annotation.Import;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.dao.OptimisticLockingFailureException;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.ResultActions;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

@WebMvcTest(GlobalExceptionHandlerTest.ThrowingController.class)
@Import({SecurityConfig.class, GlobalExceptionHandlerTest.ThrowingController.class})
class GlobalExceptionHandlerTest {

    static class MissingThing extends NotFoundException {
        MissingThing() { super("thing not found"); }
    }

    static class DuplicateThing extends ConflictException {
        DuplicateThing() { super("thing already exists"); }
    }

    static class BrokenRule extends BusinessRuleException {
        BrokenRule() { super("rule broken"); }
    }

    static class NotYours extends ForbiddenException {
        NotYours() { super("not your thing"); }
    }

    static class BadCredentials extends UnauthorizedException {
        BadCredentials() { super("bad credentials"); }
    }

    record ThingRequest(@NotBlank String name) {}

    @RestController
    static class ThrowingController {

        @GetMapping("/not-found") void notFound() { throw new MissingThing(); }

        @GetMapping("/conflict") void conflict() { throw new DuplicateThing(); }

        @GetMapping("/business-rule") void businessRule() { throw new BrokenRule(); }

        @GetMapping("/forbidden") void forbidden() { throw new NotYours(); }

        @GetMapping("/unauthorized") void unauthorized() { throw new BadCredentials(); }

        @GetMapping("/stale") void stale() { throw new OptimisticLockingFailureException("row was updated by another transaction"); }

        @GetMapping("/duplicate-key") void duplicateKey() {
            throw new DataIntegrityViolationException("Unique index or primary key violation: PUBLIC.USERS(EMAIL)");
        }

        @PostMapping("/things") void create(@Valid @RequestBody ThingRequest request) {}
    }

    @Autowired
    MockMvc mvc;

    private ResultActions getAuthenticated(String path) throws Exception {
        return mvc.perform(get(path).with(jwt()));
    }

    private static void expectProblem(ResultActions result, int status, String detail) throws Exception {
        result.andExpect(status().is(status))
                .andExpect(content().contentTypeCompatibleWith(MediaType.APPLICATION_PROBLEM_JSON))
                .andExpect(jsonPath("$.status").value(status))
                .andExpect(jsonPath("$.title").isNotEmpty())
                .andExpect(jsonPath("$.detail").value(detail));
    }

    @Test
    void mapsNotFoundTo404() throws Exception {
        var result = getAuthenticated("/not-found");

        expectProblem(result, 404, "thing not found");
    }

    @Test
    void mapsConflictTo409() throws Exception {
        var result = getAuthenticated("/conflict");

        expectProblem(result, 409, "thing already exists");
    }

    @Test
    void mapsBusinessRuleTo422() throws Exception {
        var result = getAuthenticated("/business-rule");

        expectProblem(result, 422, "rule broken");
    }

    @Test
    void mapsForbiddenTo403() throws Exception {
        var result = getAuthenticated("/forbidden");

        expectProblem(result, 403, "not your thing");
    }

    @Test
    void mapsUnauthorizedTo401() throws Exception {
        var result = getAuthenticated("/unauthorized");

        expectProblem(result, 401, "bad credentials");
    }

    @Test
    void mapsOptimisticLockFailureTo409() throws Exception {
        var result = getAuthenticated("/stale");

        result.andExpect(status().isConflict())
                .andExpect(content().contentTypeCompatibleWith(MediaType.APPLICATION_PROBLEM_JSON))
                .andExpect(jsonPath("$.detail").isNotEmpty());
    }

    @Test
    void mapsDataIntegrityViolationTo409WithoutLeakingInternals() throws Exception {
        var result = getAuthenticated("/duplicate-key");

        result.andExpect(status().isConflict())
                .andExpect(content().contentTypeCompatibleWith(MediaType.APPLICATION_PROBLEM_JSON))
                .andExpect(jsonPath("$.detail").isNotEmpty())
                .andExpect(content().string(not(containsString("USERS"))));
    }

    @Test
    void mapsBeanValidationErrorsTo400WithFieldErrors() throws Exception {
        var request = post("/things").with(jwt()).contentType(MediaType.APPLICATION_JSON).content("{\"name\":\"\"}");

        var result = mvc.perform(request);

        result.andExpect(status().isBadRequest())
                .andExpect(content().contentTypeCompatibleWith(MediaType.APPLICATION_PROBLEM_JSON))
                .andExpect(jsonPath("$.errors.name").isNotEmpty());
    }
}

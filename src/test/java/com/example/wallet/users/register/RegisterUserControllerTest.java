package com.example.wallet.users.register;

import static org.hamcrest.Matchers.containsString;
import static org.hamcrest.Matchers.not;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.header;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.example.wallet.shared.security.SecurityConfig;
import com.example.wallet.users.domain.Email;
import com.example.wallet.users.domain.User;
import java.util.UUID;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.ResultActions;

@WebMvcTest(RegisterUserController.class)
@Import(SecurityConfig.class)
class RegisterUserControllerTest {

    private static final String VALID_BODY =
            "{\"name\":\"Ana\",\"email\":\"Ana@Example.com\",\"password\":\"s3cret-pass\"}";

    @Autowired
    MockMvc mvc;

    @MockitoBean
    RegisterUserUseCase useCase;

    private ResultActions postUsers(String body) throws Exception {
        return mvc.perform(post("/users").contentType(MediaType.APPLICATION_JSON).content(body));
    }

    @Test
    void registersUserWithoutTokenAndReturnsCreatedWithoutPassword() throws Exception {
        var id = UUID.randomUUID();
        when(useCase.register(any())).thenReturn(new User(id, "Ana", new Email("ana@example.com"), "secret-hash"));

        var result = postUsers(VALID_BODY);

        result.andExpect(status().isCreated())
                .andExpect(header().string("Location", "/users/" + id))
                .andExpect(jsonPath("$.id").value(id.toString()))
                .andExpect(jsonPath("$.name").value("Ana"))
                .andExpect(jsonPath("$.email").value("ana@example.com"))
                .andExpect(jsonPath("$.password").doesNotExist())
                .andExpect(jsonPath("$.passwordHash").doesNotExist())
                .andExpect(content().string(not(containsString("s3cret-pass"))))
                .andExpect(content().string(not(containsString("secret-hash"))));
        verify(useCase).register(new RegisterUserCommand("Ana", "Ana@Example.com", "s3cret-pass"));
    }
}

package com.example.wallet.users.login;

import jakarta.validation.Valid;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

@RestController
class LoginController {

    private final LoginUseCase loginUseCase;

    LoginController(LoginUseCase loginUseCase) {
        this.loginUseCase = loginUseCase;
    }

    @PostMapping("/auth/login")
    LoginResponse login(@Valid @RequestBody LoginRequest request) {
        return LoginResponse.from(loginUseCase.login(request.toCommand()));
    }
}

package com.example.wallet.users.login;

import com.example.wallet.users.domain.PasswordHasher;
import com.example.wallet.users.domain.UserRepository;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
class LoginConfig {

    @Bean
    LoginUseCase loginUseCase(UserRepository users, PasswordHasher passwordHasher, TokenIssuer tokenIssuer) {
        return new LoginInteractor(users, passwordHasher, tokenIssuer);
    }
}

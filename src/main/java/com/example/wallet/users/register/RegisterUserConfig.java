package com.example.wallet.users.register;

import com.example.wallet.users.domain.PasswordHasher;
import com.example.wallet.users.domain.UserRepository;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
class RegisterUserConfig {

    @Bean
    RegisterUserUseCase registerUserUseCase(UserRepository users, PasswordHasher passwordHasher) {
        return new RegisterUserInteractor(users, passwordHasher);
    }
}

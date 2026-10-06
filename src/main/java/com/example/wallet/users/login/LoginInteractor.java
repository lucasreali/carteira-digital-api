package com.example.wallet.users.login;

import com.example.wallet.users.domain.Email;
import com.example.wallet.users.domain.InvalidCredentialsException;
import com.example.wallet.users.domain.PasswordHasher;
import com.example.wallet.users.domain.User;
import com.example.wallet.users.domain.UserRepository;

public class LoginInteractor implements LoginUseCase {

    private final UserRepository users;
    private final PasswordHasher passwordHasher;
    private final TokenIssuer tokenIssuer;

    public LoginInteractor(UserRepository users, PasswordHasher passwordHasher, TokenIssuer tokenIssuer) {
        this.users = users;
        this.passwordHasher = passwordHasher;
        this.tokenIssuer = tokenIssuer;
    }

    @Override
    public AccessToken login(LoginCommand command) {
        User user = users.findByEmail(new Email(command.email())).orElseThrow(InvalidCredentialsException::new);
        return tokenIssuer.issueFor(user);
    }
}

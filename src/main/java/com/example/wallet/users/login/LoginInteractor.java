package com.example.wallet.users.login;

import com.example.wallet.users.domain.Email;
import com.example.wallet.users.domain.InvalidCredentialsException;
import com.example.wallet.users.domain.InvalidEmailException;
import com.example.wallet.users.domain.PasswordHasher;
import com.example.wallet.users.domain.User;
import com.example.wallet.users.domain.UserRepository;
import java.util.Optional;

public class LoginInteractor implements LoginUseCase {

    private final UserRepository users;
    private final PasswordHasher passwordHasher;
    private final TokenIssuer tokenIssuer;
    /** Checked against when the email is unknown, so both failure paths cost one hash verification. */
    private final String dummyHash;

    public LoginInteractor(UserRepository users, PasswordHasher passwordHasher, TokenIssuer tokenIssuer) {
        this.users = users;
        this.passwordHasher = passwordHasher;
        this.tokenIssuer = tokenIssuer;
        this.dummyHash = passwordHasher.hash("dummy-password-for-timing");
    }

    @Override
    public AccessToken login(LoginCommand command) {
        Optional<User> user = findUser(command.email());
        String hash = user.map(User::passwordHash).orElse(dummyHash);
        boolean matches = passwordHasher.matches(command.password(), hash);
        if (user.isEmpty() || !matches) {
            throw new InvalidCredentialsException();
        }
        return tokenIssuer.issueFor(user.get());
    }

    /** A malformed email is just an unknown user here: login must not reveal why credentials failed. */
    private Optional<User> findUser(String rawEmail) {
        try {
            return users.findByEmail(new Email(rawEmail));
        } catch (InvalidEmailException e) {
            return Optional.empty();
        }
    }
}

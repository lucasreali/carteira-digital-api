package com.example.wallet.users.register;

import com.example.wallet.users.domain.Email;
import com.example.wallet.users.domain.EmailAlreadyRegisteredException;
import com.example.wallet.users.domain.PasswordHasher;
import com.example.wallet.users.domain.User;
import com.example.wallet.users.domain.UserRepository;

public class RegisterUserInteractor implements RegisterUserUseCase {

    private final UserRepository users;
    private final PasswordHasher passwordHasher;

    public RegisterUserInteractor(UserRepository users, PasswordHasher passwordHasher) {
        this.users = users;
        this.passwordHasher = passwordHasher;
    }

    @Override
    public User register(RegisterUserCommand command) {
        var email = new Email(command.email());
        if (users.existsByEmail(email)) {
            throw new EmailAlreadyRegisteredException();
        }
        var user = User.register(command.name(), email, passwordHasher.hash(command.password()));
        users.save(user);
        return user;
    }
}

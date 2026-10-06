package com.example.wallet.users.register;

import com.example.wallet.users.domain.User;

public interface RegisterUserUseCase {

    User register(RegisterUserCommand command);
}

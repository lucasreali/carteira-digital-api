package com.example.wallet.users.register;

import com.example.wallet.users.domain.User;
import java.util.UUID;

public record RegisterUserResponse(UUID id, String name, String email) {

    static RegisterUserResponse from(User user) {
        return new RegisterUserResponse(user.id(), user.name(), user.email().value());
    }
}

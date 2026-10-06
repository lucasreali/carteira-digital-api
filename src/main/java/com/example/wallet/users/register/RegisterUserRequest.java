package com.example.wallet.users.register;

public record RegisterUserRequest(String name, String email, String password) {

    RegisterUserCommand toCommand() {
        return new RegisterUserCommand(name, email, password);
    }
}

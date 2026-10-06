package com.example.wallet.users.register;

public record RegisterUserCommand(String name, String email, String password) {

    @Override
    public String toString() {
        return "RegisterUserCommand[name=" + name + ", email=" + email + ", password=***]";
    }
}

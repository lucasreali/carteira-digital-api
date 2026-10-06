package com.example.wallet.users.register;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record RegisterUserRequest(
        @NotBlank @Size(max = 255) String name,
        @NotBlank @Email @Size(max = 255) String email,
        // BCrypt only uses the first 72 bytes of the password (and Spring's encoder rejects longer input).
        @NotBlank @Size(min = 8) @MaxUtf8Bytes(72) String password) {

    RegisterUserCommand toCommand() {
        return new RegisterUserCommand(name, email, password);
    }
}

package com.example.wallet.users.login;

public interface LoginUseCase {

    AccessToken login(LoginCommand command);
}

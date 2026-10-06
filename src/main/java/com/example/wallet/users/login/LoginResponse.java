package com.example.wallet.users.login;

record LoginResponse(String accessToken, String tokenType, long expiresIn) {

    static LoginResponse from(AccessToken token) {
        return new LoginResponse(token.value(), "Bearer", token.expiresIn().toSeconds());
    }
}

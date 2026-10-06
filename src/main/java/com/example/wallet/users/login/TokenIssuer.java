package com.example.wallet.users.login;

import com.example.wallet.users.domain.User;

/** Output port: issues an access token proving the user's identity. */
public interface TokenIssuer {

    AccessToken issueFor(User user);
}

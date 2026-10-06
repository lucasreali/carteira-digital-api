package com.example.wallet.users.login;

import java.time.Duration;

public record AccessToken(String value, Duration expiresIn) {}

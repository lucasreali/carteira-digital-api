package com.example.wallet.users.domain;

import java.util.Optional;

public interface UserRepository {

    boolean existsByEmail(Email email);

    void save(User user);

    Optional<User> findByEmail(Email email);
}

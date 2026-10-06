package com.example.wallet.users.persistence;

import com.example.wallet.users.domain.Email;
import com.example.wallet.users.domain.User;
import com.example.wallet.users.domain.UserRepository;
import java.util.Optional;
import org.springframework.stereotype.Repository;

@Repository
public class JpaUserRepository implements UserRepository {

    private final SpringDataUserRepository users;

    JpaUserRepository(SpringDataUserRepository users) {
        this.users = users;
    }

    @Override
    public boolean existsByEmail(Email email) {
        return users.existsByEmail(email.value());
    }

    @Override
    public void save(User user) {
        users.saveAndFlush(UserJpaEntity.from(user));
    }

    @Override
    public Optional<User> findByEmail(Email email) {
        return users.findByEmail(email.value()).map(UserJpaEntity::toDomain);
    }
}

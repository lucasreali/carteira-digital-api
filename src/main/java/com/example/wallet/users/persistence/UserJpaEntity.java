package com.example.wallet.users.persistence;

import com.example.wallet.users.domain.Email;
import com.example.wallet.users.domain.User;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import jakarta.persistence.UniqueConstraint;
import java.util.UUID;

@Entity
@Table(name = "users", uniqueConstraints = @UniqueConstraint(name = "uk_users_email", columnNames = "email"))
class UserJpaEntity {

    @Id
    private UUID id;

    @Column(nullable = false)
    private String name;

    @Column(nullable = false)
    private String email;

    @Column(nullable = false)
    private String passwordHash;

    protected UserJpaEntity() {}

    static UserJpaEntity from(User user) {
        var entity = new UserJpaEntity();
        entity.id = user.id();
        entity.name = user.name();
        entity.email = user.email().value();
        entity.passwordHash = user.passwordHash();
        return entity;
    }

    User toDomain() {
        return new User(id, name, new Email(email), passwordHash);
    }
}

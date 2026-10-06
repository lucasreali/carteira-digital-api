package com.example.wallet.users.persistence;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import com.example.wallet.users.domain.Email;
import com.example.wallet.users.domain.User;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.data.jpa.test.autoconfigure.DataJpaTest;
import org.springframework.boot.jpa.test.autoconfigure.TestEntityManager;
import org.springframework.context.annotation.Import;
import org.springframework.dao.DataIntegrityViolationException;

@DataJpaTest
@Import(JpaUserRepository.class)
class JpaUserRepositoryTest {

    @Autowired
    JpaUserRepository repository;

    @Autowired
    TestEntityManager entityManager;

    @Test
    void savedUserIsFoundByEmail() {
        User user = User.register("Alice", new Email("alice@example.com"), "$2a$10$hash");

        repository.save(user);
        entityManager.clear();

        assertThat(repository.findByEmail(new Email("ALICE@example.com"))).contains(user);
    }

    @Test
    void existsByEmailReflectsSavedUsers() {
        repository.save(User.register("Alice", new Email("alice@example.com"), "$2a$10$hash"));

        boolean registered = repository.existsByEmail(new Email("alice@example.com"));
        boolean unknown = repository.existsByEmail(new Email("bob@example.com"));

        assertThat(registered).isTrue();
        assertThat(unknown).isFalse();
    }

    @Test
    void databaseRejectsSecondUserWithSameEmail() {
        repository.save(User.register("Alice", new Email("alice@example.com"), "$2a$10$hash"));
        User duplicate = User.register("Other Alice", new Email("Alice@Example.com"), "$2a$10$other");

        assertThatThrownBy(() -> repository.save(duplicate)).isInstanceOf(DataIntegrityViolationException.class);
    }
}

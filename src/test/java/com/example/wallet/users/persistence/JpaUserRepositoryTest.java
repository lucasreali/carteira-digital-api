package com.example.wallet.users.persistence;

import static org.assertj.core.api.Assertions.assertThat;

import com.example.wallet.users.domain.Email;
import com.example.wallet.users.domain.User;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.data.jpa.test.autoconfigure.DataJpaTest;
import org.springframework.boot.jpa.test.autoconfigure.TestEntityManager;
import org.springframework.context.annotation.Import;

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
}

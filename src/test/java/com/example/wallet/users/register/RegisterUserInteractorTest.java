package com.example.wallet.users.register;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.catchThrowable;

import com.example.wallet.users.domain.Email;
import com.example.wallet.users.domain.EmailAlreadyRegisteredException;
import com.example.wallet.users.domain.PasswordHasher;
import com.example.wallet.users.domain.User;
import com.example.wallet.users.domain.UserRepository;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;
import org.junit.jupiter.api.Test;

class RegisterUserInteractorTest {

    static class InMemoryUserRepository implements UserRepository {
        final Map<Email, User> users = new HashMap<>();

        @Override public boolean existsByEmail(Email email) { return users.containsKey(email); }

        @Override public void save(User user) { users.put(user.email(), user); }

        @Override public Optional<User> findByEmail(Email email) { return Optional.ofNullable(users.get(email)); }
    }

    static class PrefixPasswordHasher implements PasswordHasher {
        int hashCalls;

        @Override public String hash(String raw) { hashCalls++; return "hashed:" + raw; }

        @Override public boolean matches(String raw, String hash) { return hash.equals(hash(raw)); }
    }

    private final InMemoryUserRepository users = new InMemoryUserRepository();
    private final PrefixPasswordHasher hasher = new PrefixPasswordHasher();
    private final RegisterUserInteractor interactor = new RegisterUserInteractor(users, hasher);

    @Test
    void registersUserWithNormalizedEmailAndHashedPassword() {
        var command = new RegisterUserCommand("Ana", "  Ana@Example.COM ", "s3cret-pass");

        User created = interactor.register(command);

        assertThat(created.id()).isNotNull();
        assertThat(created.name()).isEqualTo("Ana");
        assertThat(created.email()).isEqualTo(new Email("ana@example.com"));
        assertThat(created.passwordHash()).isEqualTo("hashed:s3cret-pass");
        assertThat(users.findByEmail(new Email("ana@example.com"))).contains(created);
    }

    @Test
    void rejectsEmailAlreadyRegisteredRegardlessOfCase() {
        User existing = interactor.register(new RegisterUserCommand("Ana", "ana@example.com", "s3cret-pass"));
        var duplicate = new RegisterUserCommand("Other", "ANA@example.com", "another-pass");
        hasher.hashCalls = 0;

        var thrown = catchThrowable(() -> interactor.register(duplicate));

        assertThat(thrown).isInstanceOf(EmailAlreadyRegisteredException.class);
        assertThat(users.users.values()).containsExactly(existing);
        assertThat(hasher.hashCalls).isZero();
    }
}

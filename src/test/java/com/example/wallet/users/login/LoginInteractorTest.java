package com.example.wallet.users.login;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.catchThrowable;

import com.example.wallet.users.domain.Email;
import com.example.wallet.users.domain.InvalidCredentialsException;
import com.example.wallet.users.domain.PasswordHasher;
import com.example.wallet.users.domain.User;
import com.example.wallet.users.domain.UserRepository;
import java.time.Duration;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import org.junit.jupiter.api.Test;

class LoginInteractorTest {

    private final InMemoryUserRepository users = new InMemoryUserRepository();
    private final RecordingPasswordHasher hasher = new RecordingPasswordHasher();
    private final TokenIssuer tokenIssuer = user -> new AccessToken("token-for-" + user.id(), Duration.ofHours(1));
    private final LoginInteractor interactor = new LoginInteractor(users, hasher, tokenIssuer);

    private User registered(String email, String password) {
        User user = User.register("Alice", new Email(email), hasher.hash(password));
        users.save(user);
        return user;
    }

    @Test
    void issuesTokenWhenCredentialsMatch() {
        User alice = registered("alice@example.com", "s3cret-password");

        AccessToken token = interactor.login(new LoginCommand(" Alice@Example.com ", "s3cret-password"));

        assertThat(token).isEqualTo(new AccessToken("token-for-" + alice.id(), Duration.ofHours(1)));
    }

    @Test
    void rejectsWrongPassword() {
        registered("alice@example.com", "s3cret-password");

        Throwable thrown = catchThrowable(
                () -> interactor.login(new LoginCommand("alice@example.com", "wrong-password")));

        assertThat(thrown).isInstanceOf(InvalidCredentialsException.class);
    }

    @Test
    void rejectsUnknownEmailWithSameErrorAfterStillCheckingAPassword() {
        Throwable thrown = catchThrowable(
                () -> interactor.login(new LoginCommand("nobody@example.com", "s3cret-password")));

        assertThat(thrown)
                .isInstanceOf(InvalidCredentialsException.class)
                .hasMessage(new InvalidCredentialsException().getMessage());
        assertThat(hasher.checkedHashes).as("password check runs anyway to equalize timing").hasSize(1);
    }

    private static final class InMemoryUserRepository implements UserRepository {
        private final Map<Email, User> byEmail = new HashMap<>();

        @Override
        public boolean existsByEmail(Email email) {
            return byEmail.containsKey(email);
        }

        @Override
        public void save(User user) {
            byEmail.put(user.email(), user);
        }

        @Override
        public Optional<User> findByEmail(Email email) {
            return Optional.ofNullable(byEmail.get(email));
        }
    }

    /** Reversible fake hash; records every hash checked by {@link #matches}. */
    private static final class RecordingPasswordHasher implements PasswordHasher {
        private final List<String> checkedHashes = new ArrayList<>();

        @Override
        public String hash(String raw) {
            return "hashed:" + raw;
        }

        @Override
        public boolean matches(String raw, String hash) {
            checkedHashes.add(hash);
            return hash.equals(hash(raw));
        }
    }
}

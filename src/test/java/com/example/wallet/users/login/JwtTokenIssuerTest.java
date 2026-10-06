package com.example.wallet.users.login;

import static java.nio.charset.StandardCharsets.UTF_8;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.within;

import com.example.wallet.shared.security.JwtProperties;
import com.example.wallet.users.domain.Email;
import com.example.wallet.users.domain.User;
import java.time.Duration;
import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.UUID;
import javax.crypto.spec.SecretKeySpec;
import org.junit.jupiter.api.Test;
import org.springframework.security.oauth2.jose.jws.MacAlgorithm;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.security.oauth2.jwt.NimbusJwtDecoder;
import org.springframework.security.oauth2.jwt.NimbusJwtEncoder;

class JwtTokenIssuerTest {

    private final SecretKeySpec key = new SecretKeySpec("test-secret-with-at-least-32-bytes!!".getBytes(UTF_8), "HmacSHA256");
    private final JwtTokenIssuer issuer = new JwtTokenIssuer(
            NimbusJwtEncoder.withSecretKey(key).algorithm(MacAlgorithm.HS256).build(),
            new JwtProperties("unused-here", Duration.ofMinutes(30)));
    private final NimbusJwtDecoder decoder =
            NimbusJwtDecoder.withSecretKey(key).macAlgorithm(MacAlgorithm.HS256).build();

    @Test
    void issuesSignedTokenWithUserIdAsSubjectAndConfiguredLifetime() {
        User user = new User(UUID.randomUUID(), "Alice", new Email("alice@example.com"), "hash");

        AccessToken token = issuer.issueFor(user);

        Jwt jwt = decoder.decode(token.value());
        assertThat(token.expiresIn()).isEqualTo(Duration.ofMinutes(30));
        assertThat(jwt.getSubject()).isEqualTo(user.id().toString());
        assertThat(jwt.getClaimAsString("email")).isEqualTo("alice@example.com");
        assertThat(jwt.getIssuedAt()).isCloseTo(Instant.now(), within(5, ChronoUnit.SECONDS));
        assertThat(jwt.getExpiresAt()).isEqualTo(jwt.getIssuedAt().plus(Duration.ofMinutes(30)));
    }
}

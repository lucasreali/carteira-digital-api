package com.example.wallet.shared.security;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import java.time.Duration;
import java.time.Instant;
import org.junit.jupiter.api.Test;
import org.springframework.security.oauth2.jwt.JwtClaimsSet;
import org.springframework.security.oauth2.jwt.JwtEncoderParameters;

class JwtRoundTripTest {

    private final SecurityConfig config = new SecurityConfig();
    private final JwtProperties properties =
            new JwtProperties("test-secret-with-at-least-32-bytes!!", Duration.ofHours(1));

    @Test
    void tokenIssuedByEncoderIsAcceptedByDecoderWithSameSubject() {
        var now = Instant.now();
        var claims = JwtClaimsSet.builder().subject("user-42").issuedAt(now).expiresAt(now.plusSeconds(60)).build();

        var token = config.jwtEncoder(properties).encode(JwtEncoderParameters.from(claims)).getTokenValue();
        var decoded = config.jwtDecoder(properties).decode(token);

        assertThat(decoded.getSubject()).isEqualTo("user-42");
    }

    @Test
    void passwordEncoderHashesAndMatchesRawPassword() {
        var encoder = config.passwordEncoder();

        var hash = encoder.encode("s3cret-password");

        assertThat(hash).isNotEqualTo("s3cret-password");
        assertThat(encoder.matches("s3cret-password", hash)).isTrue();
        assertThat(encoder.matches("wrong-password", hash)).isFalse();
    }

    @Test
    void rejectsSecretShorterThan32BytesWhenCreatingJwtBeans() {
        var weak = new JwtProperties("only-31-bytes-long-secret-12345", Duration.ofHours(1));

        assertThatThrownBy(() -> config.jwtEncoder(weak))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("at least 32 bytes");
        assertThatThrownBy(() -> config.jwtDecoder(weak))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("at least 32 bytes");
    }
}

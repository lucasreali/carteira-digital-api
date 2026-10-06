package com.example.wallet.users.login;

import com.example.wallet.shared.security.JwtProperties;
import com.example.wallet.users.domain.User;
import java.time.Instant;
import org.springframework.security.oauth2.jwt.JwtClaimsSet;
import org.springframework.security.oauth2.jwt.JwtEncoder;
import org.springframework.security.oauth2.jwt.JwtEncoderParameters;
import org.springframework.stereotype.Component;

@Component
class JwtTokenIssuer implements TokenIssuer {

    private final JwtEncoder jwtEncoder;
    private final JwtProperties properties;

    JwtTokenIssuer(JwtEncoder jwtEncoder, JwtProperties properties) {
        this.jwtEncoder = jwtEncoder;
        this.properties = properties;
    }

    @Override
    public AccessToken issueFor(User user) {
        Instant now = Instant.now();
        JwtClaimsSet claims = JwtClaimsSet.builder()
                .subject(user.id().toString())
                .issuedAt(now)
                .expiresAt(now.plus(properties.tokenTtl()))
                .claim("email", user.email().value())
                .build();
        String value = jwtEncoder.encode(JwtEncoderParameters.from(claims)).getTokenValue();
        return new AccessToken(value, properties.tokenTtl());
    }
}

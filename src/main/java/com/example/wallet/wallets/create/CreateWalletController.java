package com.example.wallet.wallets.create;

import java.net.URI;
import java.util.UUID;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
class CreateWalletController {

    private final CreateWalletUseCase useCase;

    CreateWalletController(CreateWalletUseCase useCase) {
        this.useCase = useCase;
    }

    @PostMapping("/wallets")
    ResponseEntity<CreateWalletResponse> create(@AuthenticationPrincipal Jwt jwt) {
        var wallet = useCase.create(new CreateWalletCommand(UUID.fromString(jwt.getSubject())));
        return ResponseEntity.created(URI.create("/wallets/" + wallet.id()))
                .body(CreateWalletResponse.from(wallet));
    }
}

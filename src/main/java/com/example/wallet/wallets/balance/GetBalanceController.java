package com.example.wallet.wallets.balance;

import java.util.UUID;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RestController;

@RestController
class GetBalanceController {

    private final GetBalanceUseCase useCase;

    GetBalanceController(GetBalanceUseCase useCase) {
        this.useCase = useCase;
    }

    @GetMapping("/wallets/{id}/balance")
    BalanceResponse balance(@PathVariable UUID id, @AuthenticationPrincipal Jwt jwt) {
        var balance = useCase.getBalance(new GetBalanceQuery(id, UUID.fromString(jwt.getSubject())));
        return BalanceResponse.from(id, balance);
    }
}

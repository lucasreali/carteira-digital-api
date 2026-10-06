package com.example.wallet.wallets.deposit;

import com.example.wallet.wallets.web.TransactionResponse;
import java.util.UUID;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

@RestController
class DepositController {

    private final DepositUseCase useCase;

    DepositController(DepositUseCase useCase) {
        this.useCase = useCase;
    }

    @PostMapping("/wallets/{id}/deposits")
    @ResponseStatus(HttpStatus.CREATED)
    TransactionResponse deposit(
            @PathVariable UUID id, @RequestBody DepositRequest request, @AuthenticationPrincipal Jwt jwt) {
        var command = new DepositCommand(id, UUID.fromString(jwt.getSubject()), request.amount());
        return TransactionResponse.from(useCase.deposit(command));
    }
}

package com.example.wallet.wallets.withdraw;

import com.example.wallet.wallets.web.TransactionResponse;
import jakarta.validation.Valid;
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
public class WithdrawController {

    private final WithdrawUseCase withdraw;

    public WithdrawController(WithdrawUseCase withdraw) {
        this.withdraw = withdraw;
    }

    @PostMapping("/wallets/{id}/withdrawals")
    @ResponseStatus(HttpStatus.CREATED)
    TransactionResponse withdraw(
            @PathVariable UUID id, @AuthenticationPrincipal Jwt jwt, @Valid @RequestBody WithdrawRequest request) {
        var command = new WithdrawCommand(id, UUID.fromString(jwt.getSubject()), request.amount());
        return TransactionResponse.from(withdraw.withdraw(command));
    }
}

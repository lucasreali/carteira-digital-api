package com.example.wallet.wallets.transfer;

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
class TransferController {

    private final TransferUseCase transferUseCase;

    TransferController(TransferUseCase transferUseCase) {
        this.transferUseCase = transferUseCase;
    }

    @PostMapping("/wallets/{id}/transfers")
    @ResponseStatus(HttpStatus.CREATED)
    TransactionResponse transfer(
            @PathVariable UUID id, @AuthenticationPrincipal Jwt jwt, @Valid @RequestBody TransferRequest request) {
        var command = request.toCommand(id, UUID.fromString(jwt.getSubject()));
        return TransactionResponse.from(transferUseCase.transfer(command));
    }
}

package com.example.wallet.wallets.statement;

import com.example.wallet.wallets.web.TransactionResponse;
import java.util.List;
import java.util.UUID;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RestController;

@RestController
class GetStatementController {

    private final GetStatementUseCase useCase;

    GetStatementController(GetStatementUseCase useCase) {
        this.useCase = useCase;
    }

    @GetMapping("/wallets/{id}/transactions")
    List<TransactionResponse> statement(@PathVariable UUID id, @AuthenticationPrincipal Jwt jwt) {
        var query = new GetStatementQuery(id, UUID.fromString(jwt.getSubject()));
        return useCase.getStatement(query).stream().map(TransactionResponse::from).toList();
    }
}

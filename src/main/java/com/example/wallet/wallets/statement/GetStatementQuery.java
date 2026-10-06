package com.example.wallet.wallets.statement;

import java.util.UUID;

public record GetStatementQuery(UUID walletId, UUID requesterId) {
}

package com.example.wallet.wallets.balance;

import java.util.UUID;

public record GetBalanceQuery(UUID walletId, UUID requesterId) {
}

package com.example.wallet.wallets.deposit;

import java.math.BigDecimal;
import java.util.UUID;

public record DepositCommand(UUID walletId, UUID requesterId, BigDecimal amount) {}

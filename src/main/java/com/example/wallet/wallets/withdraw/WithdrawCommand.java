package com.example.wallet.wallets.withdraw;

import java.math.BigDecimal;
import java.util.UUID;

public record WithdrawCommand(UUID walletId, UUID requesterId, BigDecimal amount) {}

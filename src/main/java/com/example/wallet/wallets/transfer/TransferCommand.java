package com.example.wallet.wallets.transfer;

import java.math.BigDecimal;
import java.util.UUID;

public record TransferCommand(UUID sourceWalletId, UUID requesterId, UUID targetWalletId, BigDecimal amount) {
}

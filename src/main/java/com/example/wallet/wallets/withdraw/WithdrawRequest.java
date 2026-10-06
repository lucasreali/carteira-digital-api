package com.example.wallet.wallets.withdraw;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import java.math.BigDecimal;

record WithdrawRequest(@NotNull @Positive BigDecimal amount) {}

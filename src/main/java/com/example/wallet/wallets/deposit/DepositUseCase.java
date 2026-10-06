package com.example.wallet.wallets.deposit;

import com.example.wallet.wallets.domain.Transaction;

public interface DepositUseCase {

    Transaction deposit(DepositCommand command);
}

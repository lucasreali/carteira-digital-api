package com.example.wallet.wallets.withdraw;

import com.example.wallet.wallets.domain.Transaction;

public interface WithdrawUseCase {

    Transaction withdraw(WithdrawCommand command);
}

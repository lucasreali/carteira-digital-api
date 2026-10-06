package com.example.wallet.wallets.balance;

import com.example.wallet.wallets.domain.Money;

public interface GetBalanceUseCase {

    Money getBalance(GetBalanceQuery query);
}

package com.example.wallet.wallets.balance;

import com.example.wallet.wallets.domain.Money;
import com.example.wallet.wallets.domain.WalletNotFoundException;
import com.example.wallet.wallets.domain.WalletRepository;

public class GetBalanceInteractor implements GetBalanceUseCase {

    private final WalletRepository wallets;

    public GetBalanceInteractor(WalletRepository wallets) {
        this.wallets = wallets;
    }

    @Override
    public Money getBalance(GetBalanceQuery query) {
        return wallets.findById(query.walletId())
                .orElseThrow(() -> new WalletNotFoundException(query.walletId()))
                .balance();
    }
}

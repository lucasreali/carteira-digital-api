package com.example.wallet.wallets.statement;

import com.example.wallet.wallets.domain.Transaction;
import com.example.wallet.wallets.domain.TransactionHistory;
import com.example.wallet.wallets.domain.WalletNotFoundException;
import com.example.wallet.wallets.domain.WalletRepository;
import java.util.List;

public class GetStatementInteractor implements GetStatementUseCase {

    private final WalletRepository wallets;
    private final TransactionHistory transactionHistory;

    public GetStatementInteractor(WalletRepository wallets, TransactionHistory transactionHistory) {
        this.wallets = wallets;
        this.transactionHistory = transactionHistory;
    }

    @Override
    public List<Transaction> getStatement(GetStatementQuery query) {
        var wallet = wallets.findById(query.walletId())
                .orElseThrow(() -> new WalletNotFoundException(query.walletId()));
        wallet.ensureOwnedBy(query.requesterId());
        return transactionHistory.findByWalletId(query.walletId());
    }
}

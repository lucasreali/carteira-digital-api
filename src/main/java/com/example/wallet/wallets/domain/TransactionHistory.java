package com.example.wallet.wallets.domain;

import java.util.List;
import java.util.UUID;

public interface TransactionHistory {

    /** All transactions of the wallet, newest first. */
    List<Transaction> findByWalletId(UUID walletId);
}

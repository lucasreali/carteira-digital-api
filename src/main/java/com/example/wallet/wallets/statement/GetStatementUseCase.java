package com.example.wallet.wallets.statement;

import com.example.wallet.wallets.domain.Transaction;
import java.util.List;

public interface GetStatementUseCase {

    /** The wallet's transactions, newest first. */
    List<Transaction> getStatement(GetStatementQuery query);
}

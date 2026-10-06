package com.example.wallet.wallets.transfer;

import com.example.wallet.wallets.domain.Transaction;

public interface TransferUseCase {

    /** Moves money from the requester's wallet to any other wallet; returns the source's TRANSFER_OUT. */
    Transaction transfer(TransferCommand command);
}

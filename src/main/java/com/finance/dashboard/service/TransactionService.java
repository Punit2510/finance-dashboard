package com.finance.dashboard.service;

import com.finance.dashboard.dto.request.TransactionRequest;
import com.finance.dashboard.dto.response.PagedResponse;
import com.finance.dashboard.dto.response.TransactionResponse;
import com.finance.dashboard.enums.TransactionType;

import java.time.LocalDate;

public interface TransactionService {

    TransactionResponse createTransaction(TransactionRequest request, String username);

    TransactionResponse getTransactionById(Long id);

    PagedResponse<TransactionResponse> getTransactions(
            TransactionType type,
            String category,
            LocalDate from,
            LocalDate to,
            int page,
            int size
    );

    TransactionResponse updateTransaction(Long id, TransactionRequest request);

    void deleteTransaction(Long id);
}

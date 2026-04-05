package com.finance.dashboard.service.impl;

import com.finance.dashboard.dto.request.TransactionRequest;
import com.finance.dashboard.dto.response.PagedResponse;
import com.finance.dashboard.dto.response.TransactionResponse;
import com.finance.dashboard.entity.Transaction;
import com.finance.dashboard.entity.User;
import com.finance.dashboard.enums.TransactionType;
import com.finance.dashboard.exception.ResourceNotFoundException;
import com.finance.dashboard.repository.TransactionRepository;
import com.finance.dashboard.repository.UserRepository;
import com.finance.dashboard.service.TransactionService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;

@Service
@RequiredArgsConstructor
@Transactional
public class TransactionServiceImpl implements TransactionService {

    private final TransactionRepository transactionRepository;
    private final UserRepository userRepository;

    @Override
    public TransactionResponse createTransaction(TransactionRequest request, String username) {
        User creator = userRepository.findByUsername(username)
                .orElseThrow(() -> new UsernameNotFoundException("User not found: " + username));

        Transaction tx = Transaction.builder()
                .amount(request.getAmount())
                .type(request.getType())
                .category(request.getCategory().trim())
                .date(request.getDate())
                .notes(request.getNotes())
                .createdBy(creator)
                .build();

        return toResponse(transactionRepository.save(tx));
    }

    @Override
    @Transactional(readOnly = true)
    public TransactionResponse getTransactionById(Long id) {
        return toResponse(findOrThrow(id));
    }

    @Override
    @Transactional(readOnly = true)
    public PagedResponse<TransactionResponse> getTransactions(
            TransactionType type,
            String category,
            LocalDate from,
            LocalDate to,
            int page,
            int size) {

        Pageable pageable = PageRequest.of(page, size);
        Page<Transaction> result = transactionRepository.findAllWithFilters(type, category, from, to, pageable);
        Page<TransactionResponse> mapped = result.map(this::toResponse);
        return PagedResponse.from(mapped);
    }

    @Override
    public TransactionResponse updateTransaction(Long id, TransactionRequest request) {
        Transaction tx = findOrThrow(id);
        tx.setAmount(request.getAmount());
        tx.setType(request.getType());
        tx.setCategory(request.getCategory().trim());
        tx.setDate(request.getDate());
        tx.setNotes(request.getNotes());
        return toResponse(transactionRepository.save(tx));
    }

    @Override
    public void deleteTransaction(Long id) {
        Transaction tx = findOrThrow(id);
        tx.setDeleted(true);   // soft delete
        transactionRepository.save(tx);
    }

    // ── Helpers ────────────────────────────────────────────────────────────────

    private Transaction findOrThrow(Long id) {
        return transactionRepository.findByIdAndDeletedFalse(id)
                .orElseThrow(() -> new ResourceNotFoundException("Transaction not found with id: " + id));
    }

    public TransactionResponse toResponse(Transaction tx) {
        return TransactionResponse.builder()
                .id(tx.getId())
                .amount(tx.getAmount())
                .type(tx.getType())
                .category(tx.getCategory())
                .date(tx.getDate())
                .notes(tx.getNotes())
                .createdBy(tx.getCreatedBy().getUsername())
                .createdAt(tx.getCreatedAt())
                .updatedAt(tx.getUpdatedAt())
                .build();
    }
}

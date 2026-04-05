package com.finance.dashboard.service.impl;

import com.finance.dashboard.dto.response.DashboardSummaryResponse;
import com.finance.dashboard.dto.response.DashboardSummaryResponse.MonthlyTrend;
import com.finance.dashboard.dto.response.TransactionResponse;
import com.finance.dashboard.enums.TransactionType;
import com.finance.dashboard.repository.TransactionRepository;
import com.finance.dashboard.service.DashboardService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.*;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class DashboardServiceImpl implements DashboardService {

    private final TransactionRepository transactionRepository;
    private final TransactionServiceImpl transactionService; // reuse mapper

    @Override
    public DashboardSummaryResponse getSummary() {

        // ── Totals ─────────────────────────────────────────────────────────────
        BigDecimal totalIncome   = transactionRepository.sumByType(TransactionType.INCOME);
        BigDecimal totalExpenses = transactionRepository.sumByType(TransactionType.EXPENSE);
        BigDecimal netBalance    = totalIncome.subtract(totalExpenses);

        // ── Category breakdowns ────────────────────────────────────────────────
        Map<String, BigDecimal> incomeByCategory   = toMap(
                transactionRepository.sumGroupedByCategoryAndType(TransactionType.INCOME));
        Map<String, BigDecimal> expenseByCategory  = toMap(
                transactionRepository.sumGroupedByCategoryAndType(TransactionType.EXPENSE));

        // ── Monthly trends (last 6 months) ────────────────────────────────────
        LocalDate sixMonthsAgo = LocalDate.now().minusMonths(6).withDayOfMonth(1);
        List<Object[]> rawTrends = transactionRepository.monthlyTrends(sixMonthsAgo);
        List<MonthlyTrend> monthlyTrends = buildMonthlyTrends(rawTrends);

        // ── Recent activity (last 10) ─────────────────────────────────────────
        List<TransactionResponse> recent = transactionRepository
                .findRecentActivity(PageRequest.of(0, 10))
                .stream()
                .map(transactionService::toResponse)
                .toList();

        return DashboardSummaryResponse.builder()
                .totalIncome(totalIncome)
                .totalExpenses(totalExpenses)
                .netBalance(netBalance)
                .incomeByCategory(incomeByCategory)
                .expenseByCategory(expenseByCategory)
                .monthlyTrends(monthlyTrends)
                .recentTransactions(recent)
                .build();
    }

    // ── Private helpers ────────────────────────────────────────────────────────

    private Map<String, BigDecimal> toMap(List<Object[]> rows) {
        Map<String, BigDecimal> map = new LinkedHashMap<>();
        for (Object[] row : rows) {
            map.put((String) row[0], (BigDecimal) row[1]);
        }
        return map;
    }

    /**
     * Collapses raw JPQL rows (year, month, type, amount) into per-month summaries.
     * Each row: [Integer year, Integer month, TransactionType type, BigDecimal amount]
     */
    private List<MonthlyTrend> buildMonthlyTrends(List<Object[]> rows) {
        // key = "YYYY-MM"
        Map<String, MonthlyTrendAcc> acc = new LinkedHashMap<>();

        for (Object[] row : rows) {
            int year  = ((Number) row[0]).intValue();
            int month = ((Number) row[1]).intValue();
            TransactionType type   = (TransactionType) row[2];
            BigDecimal amount      = (BigDecimal) row[3];
            String key = year + "-" + String.format("%02d", month);

            acc.computeIfAbsent(key, k -> new MonthlyTrendAcc(year, month));
            if (type == TransactionType.INCOME) {
                acc.get(key).income = acc.get(key).income.add(amount);
            } else {
                acc.get(key).expenses = acc.get(key).expenses.add(amount);
            }
        }

        return acc.values().stream().map(a -> MonthlyTrend.builder()
                .year(a.year)
                .month(a.month)
                .income(a.income)
                .expenses(a.expenses)
                .net(a.income.subtract(a.expenses))
                .build()).toList();
    }

    private static class MonthlyTrendAcc {
        int year, month;
        BigDecimal income   = BigDecimal.ZERO;
        BigDecimal expenses = BigDecimal.ZERO;
        MonthlyTrendAcc(int y, int m) { year = y; month = m; }
    }
}

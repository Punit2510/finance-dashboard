package com.finance.dashboard.config;

import com.finance.dashboard.entity.Transaction;
import com.finance.dashboard.entity.User;
import com.finance.dashboard.enums.Role;
import com.finance.dashboard.enums.TransactionType;
import com.finance.dashboard.repository.TransactionRepository;
import com.finance.dashboard.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.jpa.repository.config.EnableJpaAuditing;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.math.BigDecimal;
import java.time.LocalDate;

@Configuration
@EnableJpaAuditing
@RequiredArgsConstructor
@Slf4j
public class AppConfig {

    @Bean
    public CommandLineRunner seedData(UserRepository userRepo,
                                      TransactionRepository txRepo,
                                      PasswordEncoder encoder) {
        return args -> {
            // ── Seed users ──────────────────────────────────────────────────────
            User admin = userRepo.save(User.builder()
                    .username("admin")
                    .email("admin@finance.com")
                    .password(encoder.encode("admin123"))
                    .role(Role.ADMIN)
                    .active(true)
                    .build());

            User analyst = userRepo.save(User.builder()
                    .username("analyst")
                    .email("analyst@finance.com")
                    .password(encoder.encode("analyst123"))
                    .role(Role.ANALYST)
                    .active(true)
                    .build());

            userRepo.save(User.builder()
                    .username("viewer")
                    .email("viewer@finance.com")
                    .password(encoder.encode("viewer123"))
                    .role(Role.VIEWER)
                    .active(true)
                    .build());

            // ── Seed transactions ────────────────────────────────────────────────
            Object[][] seeds = {
                {new BigDecimal("85000.00"), TransactionType.INCOME,  "Salary",       LocalDate.now().minusDays(2),  "Monthly salary"},
                {new BigDecimal("1200.00"),  TransactionType.EXPENSE, "Rent",         LocalDate.now().minusDays(5),  "Monthly rent"},
                {new BigDecimal("450.00"),   TransactionType.EXPENSE, "Groceries",    LocalDate.now().minusDays(7),  "Weekly groceries"},
                {new BigDecimal("5000.00"),  TransactionType.INCOME,  "Freelance",    LocalDate.now().minusDays(10), "Freelance project payment"},
                {new BigDecimal("200.00"),   TransactionType.EXPENSE, "Utilities",    LocalDate.now().minusDays(12), "Electricity bill"},
                {new BigDecimal("3500.00"),  TransactionType.INCOME,  "Investments",  LocalDate.now().minusMonths(1),"Dividend income"},
                {new BigDecimal("800.00"),   TransactionType.EXPENSE, "Travel",       LocalDate.now().minusMonths(1),"Flight tickets"},
                {new BigDecimal("600.00"),   TransactionType.EXPENSE, "Groceries",    LocalDate.now().minusMonths(1),"Monthly groceries"},
                {new BigDecimal("90000.00"), TransactionType.INCOME,  "Salary",       LocalDate.now().minusMonths(1),"Monthly salary"},
                {new BigDecimal("1500.00"),  TransactionType.EXPENSE, "Entertainment",LocalDate.now().minusMonths(2),"Concerts and dining"},
            };

            for (Object[] s : seeds) {
                txRepo.save(Transaction.builder()
                        .amount((BigDecimal) s[0])
                        .type((TransactionType) s[1])
                        .category((String) s[2])
                        .date((LocalDate) s[3])
                        .notes((String) s[4])
                        .createdBy(admin)
                        .build());
            }

            log.info("✅  Seed data loaded: 3 users, {} transactions", seeds.length);
            log.info("    admin / admin123  |  analyst / analyst123  |  viewer / viewer123");
        };
    }
}

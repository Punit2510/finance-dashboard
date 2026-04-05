package com.finance.dashboard;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.finance.dashboard.dto.request.LoginRequest;
import com.finance.dashboard.dto.request.TransactionRequest;
import com.finance.dashboard.enums.TransactionType;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;

import java.math.BigDecimal;
import java.time.LocalDate;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
class TransactionControllerTest {

    @Autowired MockMvc mockMvc;
    @Autowired ObjectMapper objectMapper;

    private String adminToken;
    private String viewerToken;
    private String analystToken;

    @BeforeEach
    void setUp() throws Exception {
        adminToken  = login("admin",   "admin123");
        viewerToken = login("viewer",  "viewer123");
        analystToken = login("analyst","analyst123");
    }

    // ── GET /api/transactions ──────────────────────────────────────────────────

    @Test
    @DisplayName("VIEWER can list transactions")
    void listTransactions_asViewer_returns200() throws Exception {
        mockMvc.perform(get("/api/transactions")
                .header("Authorization", "Bearer " + viewerToken))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content").isArray());
    }

    @Test
    @DisplayName("Unauthenticated request is rejected with 401/403")
    void listTransactions_unauthenticated_returns401() throws Exception {
        mockMvc.perform(get("/api/transactions"))
                .andExpect(status().is4xxClientError());
    }

    // ── POST /api/transactions ─────────────────────────────────────────────────

    @Test
    @DisplayName("ADMIN can create a transaction")
    void createTransaction_asAdmin_returns201() throws Exception {
        TransactionRequest req = validRequest();

        mockMvc.perform(post("/api/transactions")
                .header("Authorization", "Bearer " + adminToken)
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(req)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id").exists())
                .andExpect(jsonPath("$.category").value("Salary"));
    }

    @Test
    @DisplayName("VIEWER cannot create a transaction — returns 403")
    void createTransaction_asViewer_returns403() throws Exception {
        mockMvc.perform(post("/api/transactions")
                .header("Authorization", "Bearer " + viewerToken)
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(validRequest())))
                .andExpect(status().isForbidden());
    }

    @Test
    @DisplayName("ANALYST cannot create a transaction — returns 403")
    void createTransaction_asAnalyst_returns403() throws Exception {
        mockMvc.perform(post("/api/transactions")
                .header("Authorization", "Bearer " + analystToken)
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(validRequest())))
                .andExpect(status().isForbidden());
    }

    @Test
    @DisplayName("Negative amount returns 400 validation error")
    void createTransaction_negativeAmount_returns400() throws Exception {
        TransactionRequest req = validRequest();
        req.setAmount(new BigDecimal("-100.00"));

        mockMvc.perform(post("/api/transactions")
                .header("Authorization", "Bearer " + adminToken)
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(req)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.fieldErrors.amount").exists());
    }

    // ── Filter tests ───────────────────────────────────────────────────────────

    @Test
    @DisplayName("Filter by type=INCOME returns only income records")
    void listTransactions_filterByType_returnsFiltered() throws Exception {
        mockMvc.perform(get("/api/transactions")
                .header("Authorization", "Bearer " + adminToken)
                .param("type", "INCOME"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content[0].type").value("INCOME"));
    }

    @Test
    @DisplayName("Filter by date range works correctly")
    void listTransactions_filterByDateRange_returns200() throws Exception {
        mockMvc.perform(get("/api/transactions")
                .header("Authorization", "Bearer " + adminToken)
                .param("from", LocalDate.now().minusDays(30).toString())
                .param("to",   LocalDate.now().toString()))
                .andExpect(status().isOk());
    }

    // ── DELETE ─────────────────────────────────────────────────────────────────

    @Test
    @DisplayName("ADMIN can soft-delete a transaction")
    void deleteTransaction_asAdmin_returns204() throws Exception {
        // First create one
        TransactionRequest req = validRequest();
        MvcResult result = mockMvc.perform(post("/api/transactions")
                .header("Authorization", "Bearer " + adminToken)
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(req)))
                .andReturn();

        String body = result.getResponse().getContentAsString();
        Long id = objectMapper.readTree(body).get("id").asLong();

        // Delete it
        mockMvc.perform(delete("/api/transactions/" + id)
                .header("Authorization", "Bearer " + adminToken))
                .andExpect(status().isNoContent());

        // Verify it is gone
        mockMvc.perform(get("/api/transactions/" + id)
                .header("Authorization", "Bearer " + adminToken))
                .andExpect(status().isNotFound());
    }

    // ── Helpers ────────────────────────────────────────────────────────────────

    private String login(String username, String password) throws Exception {
        LoginRequest req = new LoginRequest();
        req.setUsername(username);
        req.setPassword(password);
        MvcResult result = mockMvc.perform(post("/api/auth/login")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(req)))
                .andReturn();
        return objectMapper.readTree(result.getResponse().getContentAsString())
                .get("token").asText();
    }

    private TransactionRequest validRequest() {
        TransactionRequest req = new TransactionRequest();
        req.setAmount(new BigDecimal("5000.00"));
        req.setType(TransactionType.INCOME);
        req.setCategory("Salary");
        req.setDate(LocalDate.now());
        req.setNotes("Test transaction");
        return req;
    }
}

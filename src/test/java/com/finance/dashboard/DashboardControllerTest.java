package com.finance.dashboard;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.finance.dashboard.dto.request.LoginRequest;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;
import static org.springframework.http.MediaType.APPLICATION_JSON;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
class DashboardControllerTest {

    @Autowired MockMvc mockMvc;
    @Autowired ObjectMapper objectMapper;

    private String adminToken;
    private String analystToken;
    private String viewerToken;

    @BeforeEach
    void setUp() throws Exception {
        adminToken   = login("admin",   "admin123");
        analystToken = login("analyst", "analyst123");
        viewerToken  = login("viewer",  "viewer123");
    }

    @Test
    @DisplayName("ADMIN can access dashboard summary")
    void summary_asAdmin_returns200WithAllFields() throws Exception {
        mockMvc.perform(get("/api/dashboard/summary")
                .header("Authorization", "Bearer " + adminToken))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.totalIncome").exists())
                .andExpect(jsonPath("$.totalExpenses").exists())
                .andExpect(jsonPath("$.netBalance").exists())
                .andExpect(jsonPath("$.incomeByCategory").isMap())
                .andExpect(jsonPath("$.expenseByCategory").isMap())
                .andExpect(jsonPath("$.monthlyTrends").isArray())
                .andExpect(jsonPath("$.recentTransactions").isArray());
    }

    @Test
    @DisplayName("ANALYST can access dashboard summary")
    void summary_asAnalyst_returns200() throws Exception {
        mockMvc.perform(get("/api/dashboard/summary")
                .header("Authorization", "Bearer " + analystToken))
                .andExpect(status().isOk());
    }

    @Test
    @DisplayName("VIEWER cannot access dashboard summary — returns 403")
    void summary_asViewer_returns403() throws Exception {
        mockMvc.perform(get("/api/dashboard/summary")
                .header("Authorization", "Bearer " + viewerToken))
                .andExpect(status().isForbidden());
    }

    @Test
    @DisplayName("Unauthenticated request to dashboard is rejected")
    void summary_unauthenticated_returns401or403() throws Exception {
        mockMvc.perform(get("/api/dashboard/summary"))
                .andExpect(status().is4xxClientError());
    }

    private String login(String username, String password) throws Exception {
        LoginRequest req = new LoginRequest();
        req.setUsername(username);
        req.setPassword(password);
        MvcResult result = mockMvc.perform(post("/api/auth/login")
                .contentType(APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(req)))
                .andReturn();
        return objectMapper.readTree(result.getResponse().getContentAsString())
                .get("token").asText();
    }
}

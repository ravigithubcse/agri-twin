package com.agritwin.user.integration;

import com.agritwin.user.dto.LoginRequest;
import com.agritwin.user.dto.RegisterRequest;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;

import static org.hamcrest.Matchers.notNullValue;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

/**
 * End-to-end test of the full auth flow through the real Spring MVC stack
 * (controller -> service -> repository -> H2 in-memory DB), not mocks.
 * This is what actually proves register -> login -> /me -> refresh works
 * as a wired system, not just that individual units behave in isolation.
 */
@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
@DisplayName("Auth flow (full integration)")
class AuthFlowIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @Test
    @DisplayName("register -> login -> /me -> refresh -> logout works end to end")
    void fullAuthLifecycle_works() throws Exception {
        RegisterRequest registerRequest = new RegisterRequest(
                "9123456780", "MySecurePass1", "Lakshmi Devi", "MH", "PUN", "mr");

        String registerResponseJson = mockMvc.perform(post("/api/v1/auth/register")
                        .contentType("application/json")
                        .content(objectMapper.writeValueAsString(registerRequest)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.accessToken", notNullValue()))
                .andExpect(jsonPath("$.refreshToken", notNullValue()))
                .andExpect(jsonPath("$.user.phone").value("9123456780"))
                .andExpect(jsonPath("$.user.fullName").value("Lakshmi Devi"))
                .andReturn().getResponse().getContentAsString();

        String accessToken = objectMapper.readTree(registerResponseJson).get("accessToken").asText();
        String refreshToken = objectMapper.readTree(registerResponseJson).get("refreshToken").asText();

        // Duplicate registration must be rejected
        mockMvc.perform(post("/api/v1/auth/register")
                        .contentType("application/json")
                        .content(objectMapper.writeValueAsString(registerRequest)))
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$.error").value("CONFLICT"));

        // /me requires the access token
        mockMvc.perform(get("/api/v1/users/me")
                        .header("Authorization", "Bearer " + accessToken))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.phone").value("9123456780"));

        // /me without a token is rejected
        mockMvc.perform(get("/api/v1/users/me"))
                .andExpect(status().isUnauthorized());

        // Login with correct credentials
        LoginRequest loginRequest = new LoginRequest("9123456780", "MySecurePass1");
        mockMvc.perform(post("/api/v1/auth/login")
                        .contentType("application/json")
                        .content(objectMapper.writeValueAsString(loginRequest)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.accessToken", notNullValue()));

        // Login with wrong password is rejected
        mockMvc.perform(post("/api/v1/auth/login")
                        .contentType("application/json")
                        .content(objectMapper.writeValueAsString(
                                new LoginRequest("9123456780", "WrongPassword"))))
                .andExpect(status().isUnauthorized());

        // Refresh token exchange works
        String refreshBody = objectMapper.writeValueAsString(new java.util.HashMap<>() {{
            put("refreshToken", refreshToken);
        }});
        mockMvc.perform(post("/api/v1/auth/refresh")
                        .contentType("application/json")
                        .content(refreshBody))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.accessToken", notNullValue()));

        // Logout revokes refresh tokens
        mockMvc.perform(post("/api/v1/auth/logout")
                        .header("Authorization", "Bearer " + accessToken))
                .andExpect(status().isNoContent());

        // After logout, the old refresh token can no longer be used
        mockMvc.perform(post("/api/v1/auth/refresh")
                        .contentType("application/json")
                        .content(refreshBody))
                .andExpect(status().isUnauthorized());
    }

    @Test
    @DisplayName("register rejects invalid phone number format")
    void register_rejectsInvalidPhone() throws Exception {
        RegisterRequest invalid = new RegisterRequest("12345", "MySecurePass1", "Test User", null, null, null);

        mockMvc.perform(post("/api/v1/auth/register")
                        .contentType("application/json")
                        .content(objectMapper.writeValueAsString(invalid)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.error").value("VALIDATION_ERROR"));
    }

    @Test
    @DisplayName("register rejects short password")
    void register_rejectsShortPassword() throws Exception {
        RegisterRequest invalid = new RegisterRequest("9123456781", "short", "Test User", null, null, null);

        mockMvc.perform(post("/api/v1/auth/register")
                        .contentType("application/json")
                        .content(objectMapper.writeValueAsString(invalid)))
                .andExpect(status().isBadRequest());
    }
}

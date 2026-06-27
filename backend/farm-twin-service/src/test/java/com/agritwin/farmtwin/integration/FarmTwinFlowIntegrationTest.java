package com.agritwin.farmtwin.integration;

import com.agritwin.farmtwin.dto.CropHistoryRequest;
import com.agritwin.farmtwin.dto.LandParcelRequest;
import com.fasterxml.jackson.databind.ObjectMapper;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;

import javax.crypto.SecretKey;
import java.math.BigDecimal;
import java.time.Instant;
import java.time.LocalDate;
import java.util.Date;
import java.util.UUID;

import static org.hamcrest.Matchers.notNullValue;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

/**
 * Mints its own JWTs using the same test secret configured in
 * application-test.yml, simulating tokens that would in production be
 * issued by user-service. This proves farm-twin-service's verify-only trust
 * model actually works end-to-end against the real Spring Security filter
 * chain, not just that the controller logic is correct in isolation.
 */
@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
@DisplayName("Farm twin flow (full integration)")
class FarmTwinFlowIntegrationTest {

    private static final String TEST_SECRET =
            "test-only-secret-key-not-for-production-use-min-256-bits-xxxxxxxxxxxxxxxxxxx";

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    private String tokenFor(UUID userId) {
        SecretKey key = Keys.hmacShaKeyFor(TEST_SECRET.getBytes());
        Instant now = Instant.now();
        return Jwts.builder()
                .subject(userId.toString())
                .issuer("agritwin-ai-test")
                .claim("phone", "9123456780")
                .claim("role", "FARMER")
                .issuedAt(Date.from(now))
                .expiration(Date.from(now.plusSeconds(900)))
                .signWith(key)
                .compact();
    }

    @Test
    @DisplayName("create twin -> add land parcel -> log crop history -> fetch twin shows updated completeness")
    void fullFarmTwinLifecycle_works() throws Exception {
        UUID userId = UUID.randomUUID();
        String token = tokenFor(userId);

        // No twin yet -> 404
        mockMvc.perform(get("/api/v1/farm-twins/me").header("Authorization", "Bearer " + token))
                .andExpect(status().isNotFound());

        // Create twin
        String createTwinJson = mockMvc.perform(post("/api/v1/farm-twins/me")
                        .header("Authorization", "Bearer " + token))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.userId").value(userId.toString()))
                .andExpect(jsonPath("$.profileCompletenessScore").value(0))
                .andReturn().getResponse().getContentAsString();

        UUID twinId = UUID.fromString(objectMapper.readTree(createTwinJson).get("id").asText());
        assertNotNullJson(twinId);

        // Duplicate creation rejected
        mockMvc.perform(post("/api/v1/farm-twins/me").header("Authorization", "Bearer " + token))
                .andExpect(status().isConflict());

        // Add a land parcel
        LandParcelRequest parcelRequest = new LandParcelRequest(
                "North Field", new BigDecimal("18.520430"), new BigDecimal("73.856743"),
                "Black Cotton Soil", "Drip", new BigDecimal("1.250"), "Rice",
                LocalDate.of(2026, 6, 1), LocalDate.of(2026, 10, 15));

        String parcelJson = mockMvc.perform(post("/api/v1/farm-twins/me/land-parcels")
                        .header("Authorization", "Bearer " + token)
                        .contentType("application/json")
                        .content(objectMapper.writeValueAsString(parcelRequest)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.label").value("North Field"))
                .andExpect(jsonPath("$.areaAcres").value(1.25))
                .andReturn().getResponse().getContentAsString();

        UUID parcelId = UUID.fromString(objectMapper.readTree(parcelJson).get("id").asText());

        // Twin completeness should now be 70 (parcels present + current crop present)
        mockMvc.perform(get("/api/v1/farm-twins/me").header("Authorization", "Bearer " + token))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.profileCompletenessScore").value(70));

        // Log crop history
        CropHistoryRequest cropRequest = new CropHistoryRequest(
                "Rice", com.agritwin.farmtwin.entity.Season.KHARIF,
                new BigDecimal("18.50"), new BigDecimal("42000.00"), new BigDecimal("12000.00"),
                "Pune APMC", LocalDate.of(2026, 10, 20));

        mockMvc.perform(post("/api/v1/farm-twins/me/land-parcels/" + parcelId + "/crop-history")
                        .header("Authorization", "Bearer " + token)
                        .contentType("application/json")
                        .content(objectMapper.writeValueAsString(cropRequest)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.cropName").value("Rice"))
                .andExpect(jsonPath("$.incomeInr").value(42000.00));

        // Completeness should now be 100
        mockMvc.perform(get("/api/v1/farm-twins/me").header("Authorization", "Bearer " + token))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.profileCompletenessScore").value(100));

        // A different user cannot see this user's parcel
        String otherUserToken = tokenFor(UUID.randomUUID());
        mockMvc.perform(get("/api/v1/farm-twins/me/land-parcels").header("Authorization", "Bearer " + otherUserToken))
                .andExpect(status().isNotFound()); // other user has no twin at all yet

        // Request with no token at all is rejected
        mockMvc.perform(get("/api/v1/farm-twins/me"))
                .andExpect(status().isUnauthorized());
    }

    @Test
    @DisplayName("rejects land parcel creation with non-positive area")
    void createParcel_rejectsNonPositiveArea() throws Exception {
        UUID userId = UUID.randomUUID();
        String token = tokenFor(userId);

        mockMvc.perform(post("/api/v1/farm-twins/me").header("Authorization", "Bearer " + token))
                .andExpect(status().isCreated());

        LandParcelRequest invalid = new LandParcelRequest(
                "Bad Field", null, null, null, null, new BigDecimal("0"), null, null, null);

        mockMvc.perform(post("/api/v1/farm-twins/me/land-parcels")
                        .header("Authorization", "Bearer " + token)
                        .contentType("application/json")
                        .content(objectMapper.writeValueAsString(invalid)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.error").value("VALIDATION_ERROR"));
    }

    private void assertNotNullJson(UUID value) {
        org.assertj.core.api.Assertions.assertThat(value).isNotNull();
    }
}

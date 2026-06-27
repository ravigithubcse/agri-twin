package com.agritwin.farmtwin.service;

import com.agritwin.farmtwin.dto.FarmTwinResponse;
import com.agritwin.farmtwin.entity.FarmTwin;
import com.agritwin.farmtwin.exception.FarmTwinAlreadyExistsException;
import com.agritwin.farmtwin.exception.FarmTwinNotFoundException;
import com.agritwin.farmtwin.repository.FarmTwinRepository;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.Instant;
import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("FarmTwinService")
class FarmTwinServiceTest {

    @Mock
    private FarmTwinRepository farmTwinRepository;
    @Mock
    private FarmTwinMapper farmTwinMapper;

    @InjectMocks
    private FarmTwinService farmTwinService;

    @Test
    @DisplayName("createForUser() creates a new twin when none exists")
    void createForUser_createsTwin_whenNoneExists() {
        UUID userId = UUID.randomUUID();
        FarmTwin saved = FarmTwin.builder().id(UUID.randomUUID()).userId(userId)
                .version(1).profileCompletenessScore((short) 0).createdAt(Instant.now()).lastUpdated(Instant.now())
                .build();

        when(farmTwinRepository.existsByUserId(userId)).thenReturn(false);
        when(farmTwinRepository.save(any(FarmTwin.class))).thenReturn(saved);
        when(farmTwinMapper.toResponse(saved)).thenReturn(
                new FarmTwinResponse(saved.getId(), userId, 1, (short) 0, null, saved.getCreatedAt(), saved.getLastUpdated()));

        FarmTwinResponse response = farmTwinService.createForUser(userId);

        assertThat(response.userId()).isEqualTo(userId);
        verify(farmTwinRepository).save(any(FarmTwin.class));
    }

    @Test
    @DisplayName("createForUser() throws when a twin already exists for the user")
    void createForUser_throws_whenTwinAlreadyExists() {
        UUID userId = UUID.randomUUID();
        when(farmTwinRepository.existsByUserId(userId)).thenReturn(true);

        assertThatThrownBy(() -> farmTwinService.createForUser(userId))
                .isInstanceOf(FarmTwinAlreadyExistsException.class);

        verify(farmTwinRepository, never()).save(any());
    }

    @Test
    @DisplayName("getForUser() throws FarmTwinNotFoundException when no twin exists")
    void getForUser_throws_whenNoTwinExists() {
        UUID userId = UUID.randomUUID();
        when(farmTwinRepository.findByUserId(userId)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> farmTwinService.getForUser(userId))
                .isInstanceOf(FarmTwinNotFoundException.class);
    }

    @Test
    @DisplayName("getOwnedTwinOrThrow() returns the twin when it belongs to the caller")
    void getOwnedTwinOrThrow_returnsTwin_whenOwnedByCaller() {
        UUID userId = UUID.randomUUID();
        FarmTwin twin = FarmTwin.builder().id(UUID.randomUUID()).userId(userId).build();
        when(farmTwinRepository.findByUserId(userId)).thenReturn(Optional.of(twin));

        FarmTwin result = farmTwinService.getOwnedTwinOrThrow(userId);

        assertThat(result.getUserId()).isEqualTo(userId);
    }

    @Test
    @DisplayName("recalculateCompleteness() scores 100 when parcels, crop, and history all present")
    void recalculateCompleteness_scoresFull_whenAllSectionsPresent() {
        FarmTwin twin = FarmTwin.builder().id(UUID.randomUUID()).userId(UUID.randomUUID()).build();
        var parcel = com.agritwin.farmtwin.entity.LandParcel.builder()
                .id(UUID.randomUUID())
                .currentCrop("Rice")
                .areaAcres(new java.math.BigDecimal("1.5"))
                .build();
        var crop = com.agritwin.farmtwin.entity.CropHistory.builder()
                .id(UUID.randomUUID())
                .cropName("Rice")
                .season(com.agritwin.farmtwin.entity.Season.KHARIF)
                .build();
        parcel.getCropHistory().add(crop);
        twin.getLandParcels().add(parcel);

        when(farmTwinRepository.save(any(FarmTwin.class))).thenReturn(twin);

        farmTwinService.recalculateCompleteness(twin);

        assertThat(twin.getProfileCompletenessScore()).isEqualTo((short) 100);
    }

    @Test
    @DisplayName("recalculateCompleteness() scores 0 when twin has no parcels")
    void recalculateCompleteness_scoresZero_whenNoParcels() {
        FarmTwin twin = FarmTwin.builder().id(UUID.randomUUID()).userId(UUID.randomUUID()).build();
        when(farmTwinRepository.save(any(FarmTwin.class))).thenReturn(twin);

        farmTwinService.recalculateCompleteness(twin);

        assertThat(twin.getProfileCompletenessScore()).isEqualTo((short) 0);
    }
}

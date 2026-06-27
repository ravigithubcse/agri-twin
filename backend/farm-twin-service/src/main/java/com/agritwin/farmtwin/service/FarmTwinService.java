package com.agritwin.farmtwin.service;

import com.agritwin.farmtwin.dto.FarmTwinResponse;
import com.agritwin.farmtwin.entity.FarmTwin;
import com.agritwin.farmtwin.exception.AccessDeniedToResourceException;
import com.agritwin.farmtwin.exception.FarmTwinAlreadyExistsException;
import com.agritwin.farmtwin.exception.FarmTwinNotFoundException;
import com.agritwin.farmtwin.repository.FarmTwinRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.UUID;

/**
 * Owns the FarmTwin (FCDT) root lifecycle: creation (one per user) and
 * profile-completeness scoring. Land parcels and crop history are managed
 * by their own services but always routed through a twin owned by the
 * caller — see getOwnedTwinOrThrow, the single chokepoint that enforces
 * "resource-level permissions enforced per user_id" (blueprint 5.5).
 */
@Service
public class FarmTwinService {

    private final FarmTwinRepository farmTwinRepository;
    private final FarmTwinMapper farmTwinMapper;

    public FarmTwinService(FarmTwinRepository farmTwinRepository, FarmTwinMapper farmTwinMapper) {
        this.farmTwinRepository = farmTwinRepository;
        this.farmTwinMapper = farmTwinMapper;
    }

    @Transactional
    public FarmTwinResponse createForUser(UUID userId) {
        if (farmTwinRepository.existsByUserId(userId)) {
            throw new FarmTwinAlreadyExistsException(userId);
        }

        FarmTwin twin = FarmTwin.builder()
                .userId(userId)
                .build();

        twin = farmTwinRepository.save(twin);
        return farmTwinMapper.toResponse(twin);
    }

    @Transactional(readOnly = true)
    public FarmTwinResponse getForUser(UUID userId) {
        FarmTwin twin = farmTwinRepository.findByUserId(userId)
                .orElseThrow(() -> new FarmTwinNotFoundException(userId));
        return farmTwinMapper.toResponse(twin);
    }

    /**
     * Loads the twin owned by callerUserId. Used internally by
     * LandParcelService/CropHistoryService so every write to a parcel or
     * crop record is provably scoped to the caller's own twin.
     */
    @Transactional(readOnly = true)
    public FarmTwin getOwnedTwinOrThrow(UUID callerUserId) {
        FarmTwin twin = farmTwinRepository.findByUserId(callerUserId)
                .orElseThrow(() -> new FarmTwinNotFoundException(callerUserId));

        if (!twin.getUserId().equals(callerUserId)) {
            // Defensive check — findByUserId already filters by userId, so this
            // branch should be unreachable, but it documents the invariant
            // explicitly rather than relying solely on the query shape.
            throw new AccessDeniedToResourceException();
        }
        return twin;
    }

    /**
     * Recomputes profile_completeness_score as a simple weighted count of
     * populated sections. This is intentionally a plain heuristic, not an
     * ML model — blueprint 7.3 just needs *some* number to drive the
     * dashboard's "Farm Health Score" gauge; a learned completeness/health
     * score is a separate, later concern from a different module.
     */
    @Transactional
    public void recalculateCompleteness(FarmTwin twin) {
        short score = 0;
        if (!twin.getLandParcels().isEmpty()) {
            score += 40;
        }
        boolean anyParcelHasCrop = twin.getLandParcels().stream()
                .anyMatch(p -> p.getCurrentCrop() != null && !p.getCurrentCrop().isBlank());
        if (anyParcelHasCrop) {
            score += 30;
        }
        boolean anyParcelHasHistory = twin.getLandParcels().stream()
                .anyMatch(p -> !p.getCropHistory().isEmpty());
        if (anyParcelHasHistory) {
            score += 30;
        }
        twin.setProfileCompletenessScore(score);
        farmTwinRepository.save(twin);
    }
}

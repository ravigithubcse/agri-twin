package com.agritwin.farmtwin.service;

import com.agritwin.farmtwin.dto.LandParcelRequest;
import com.agritwin.farmtwin.dto.LandParcelResponse;
import com.agritwin.farmtwin.entity.FarmTwin;
import com.agritwin.farmtwin.entity.LandParcel;
import com.agritwin.farmtwin.exception.LandParcelNotFoundException;
import com.agritwin.farmtwin.repository.LandParcelRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.UUID;

@Service
public class LandParcelService {

    private final LandParcelRepository landParcelRepository;
    private final FarmTwinService farmTwinService;
    private final FarmTwinMapper farmTwinMapper;

    public LandParcelService(LandParcelRepository landParcelRepository,
                              FarmTwinService farmTwinService,
                              FarmTwinMapper farmTwinMapper) {
        this.landParcelRepository = landParcelRepository;
        this.farmTwinService = farmTwinService;
        this.farmTwinMapper = farmTwinMapper;
    }

    @Transactional
    public LandParcelResponse create(UUID callerUserId, LandParcelRequest request) {
        FarmTwin twin = farmTwinService.getOwnedTwinOrThrow(callerUserId);

        LandParcel parcel = LandParcel.builder()
                .farmTwin(twin)
                .label(request.label())
                .latitude(request.latitude())
                .longitude(request.longitude())
                .soilType(request.soilType())
                .irrigationType(request.irrigationType())
                .areaAcres(request.areaAcres())
                .currentCrop(request.currentCrop())
                .sowingDate(request.sowingDate())
                .expectedHarvestDate(request.expectedHarvestDate())
                .build();

        parcel = landParcelRepository.save(parcel);
        twin.getLandParcels().add(parcel);
        farmTwinService.recalculateCompleteness(twin);

        return farmTwinMapper.toResponse(parcel);
    }

    @Transactional(readOnly = true)
    public List<LandParcelResponse> listForCaller(UUID callerUserId) {
        FarmTwin twin = farmTwinService.getOwnedTwinOrThrow(callerUserId);
        return landParcelRepository.findByFarmTwinId(twin.getId()).stream()
                .map(farmTwinMapper::toResponse)
                .toList();
    }

    @Transactional(readOnly = true)
    public LandParcelResponse getOneForCaller(UUID callerUserId, UUID parcelId) {
        FarmTwin twin = farmTwinService.getOwnedTwinOrThrow(callerUserId);
        LandParcel parcel = landParcelRepository.findByIdAndFarmTwinId(parcelId, twin.getId())
                .orElseThrow(() -> new LandParcelNotFoundException(parcelId));
        return farmTwinMapper.toResponse(parcel);
    }

    /**
     * Loads a parcel ensuring it belongs to callerUserId's twin. Used by
     * CropHistoryService so crop records can never be attached to a parcel
     * the caller doesn't own.
     */
    @Transactional(readOnly = true)
    public LandParcel getOwnedParcelOrThrow(UUID callerUserId, UUID parcelId) {
        FarmTwin twin = farmTwinService.getOwnedTwinOrThrow(callerUserId);
        return landParcelRepository.findByIdAndFarmTwinId(parcelId, twin.getId())
                .orElseThrow(() -> new LandParcelNotFoundException(parcelId));
    }
}

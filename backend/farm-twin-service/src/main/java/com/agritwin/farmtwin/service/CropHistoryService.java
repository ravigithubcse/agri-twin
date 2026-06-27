package com.agritwin.farmtwin.service;

import com.agritwin.farmtwin.dto.CropHistoryRequest;
import com.agritwin.farmtwin.dto.CropHistoryResponse;
import com.agritwin.farmtwin.entity.CropHistory;
import com.agritwin.farmtwin.entity.FarmTwin;
import com.agritwin.farmtwin.entity.LandParcel;
import com.agritwin.farmtwin.exception.CropHistoryNotFoundException;
import com.agritwin.farmtwin.repository.CropHistoryRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.UUID;

@Service
public class CropHistoryService {

    private final CropHistoryRepository cropHistoryRepository;
    private final LandParcelService landParcelService;
    private final FarmTwinService farmTwinService;
    private final FarmTwinMapper farmTwinMapper;

    public CropHistoryService(CropHistoryRepository cropHistoryRepository,
                               LandParcelService landParcelService,
                               FarmTwinService farmTwinService,
                               FarmTwinMapper farmTwinMapper) {
        this.cropHistoryRepository = cropHistoryRepository;
        this.landParcelService = landParcelService;
        this.farmTwinService = farmTwinService;
        this.farmTwinMapper = farmTwinMapper;
    }

    @Transactional
    public CropHistoryResponse create(UUID callerUserId, UUID parcelId, CropHistoryRequest request) {
        LandParcel parcel = landParcelService.getOwnedParcelOrThrow(callerUserId, parcelId);

        CropHistory record = CropHistory.builder()
                .landParcel(parcel)
                .cropName(request.cropName())
                .season(request.season())
                .yieldQuintals(request.yieldQuintals())
                .incomeInr(request.incomeInr())
                .inputCostInr(request.inputCostInr())
                .marketName(request.marketName())
                .saleDate(request.saleDate())
                .build();

        record = cropHistoryRepository.save(record);
        parcel.getCropHistory().add(record);

        FarmTwin twin = farmTwinService.getOwnedTwinOrThrow(callerUserId);
        farmTwinService.recalculateCompleteness(twin);

        return farmTwinMapper.toResponse(record);
    }

    @Transactional(readOnly = true)
    public List<CropHistoryResponse> listForParcel(UUID callerUserId, UUID parcelId) {
        LandParcel parcel = landParcelService.getOwnedParcelOrThrow(callerUserId, parcelId);
        return cropHistoryRepository.findByLandParcelId(parcel.getId()).stream()
                .map(farmTwinMapper::toResponse)
                .toList();
    }

    @Transactional(readOnly = true)
    public CropHistoryResponse getOneForParcel(UUID callerUserId, UUID parcelId, UUID cropHistoryId) {
        LandParcel parcel = landParcelService.getOwnedParcelOrThrow(callerUserId, parcelId);
        CropHistory record = cropHistoryRepository.findByIdAndLandParcelId(cropHistoryId, parcel.getId())
                .orElseThrow(() -> new CropHistoryNotFoundException(cropHistoryId));
        return farmTwinMapper.toResponse(record);
    }
}

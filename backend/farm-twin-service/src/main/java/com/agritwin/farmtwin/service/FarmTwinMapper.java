package com.agritwin.farmtwin.service;

import com.agritwin.farmtwin.dto.CropHistoryResponse;
import com.agritwin.farmtwin.dto.FarmTwinResponse;
import com.agritwin.farmtwin.dto.LandParcelResponse;
import com.agritwin.farmtwin.entity.CropHistory;
import com.agritwin.farmtwin.entity.FarmTwin;
import com.agritwin.farmtwin.entity.LandParcel;
import org.mapstruct.Mapper;

@Mapper(componentModel = "spring")
public interface FarmTwinMapper {

    FarmTwinResponse toResponse(FarmTwin farmTwin);

    LandParcelResponse toResponse(LandParcel landParcel);

    CropHistoryResponse toResponse(CropHistory cropHistory);
}

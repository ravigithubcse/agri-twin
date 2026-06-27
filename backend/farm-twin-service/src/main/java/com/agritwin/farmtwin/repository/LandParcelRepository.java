package com.agritwin.farmtwin.repository;

import com.agritwin.farmtwin.entity.LandParcel;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface LandParcelRepository extends JpaRepository<LandParcel, UUID> {

    List<LandParcel> findByFarmTwinId(UUID farmTwinId);

    Optional<LandParcel> findByIdAndFarmTwinId(UUID id, UUID farmTwinId);
}

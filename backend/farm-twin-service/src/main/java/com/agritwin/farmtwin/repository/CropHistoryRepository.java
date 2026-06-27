package com.agritwin.farmtwin.repository;

import com.agritwin.farmtwin.entity.CropHistory;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface CropHistoryRepository extends JpaRepository<CropHistory, UUID> {

    List<CropHistory> findByLandParcelId(UUID landParcelId);

    Optional<CropHistory> findByIdAndLandParcelId(UUID id, UUID landParcelId);
}

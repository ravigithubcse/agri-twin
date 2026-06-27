package com.agritwin.farmtwin.repository;

import com.agritwin.farmtwin.entity.FarmTwin;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;
import java.util.UUID;

public interface FarmTwinRepository extends JpaRepository<FarmTwin, UUID> {

    Optional<FarmTwin> findByUserId(UUID userId);

    boolean existsByUserId(UUID userId);
}

package com.agritwin.farmtwin.controller;

import com.agritwin.farmtwin.dto.CropHistoryRequest;
import com.agritwin.farmtwin.dto.CropHistoryResponse;
import com.agritwin.farmtwin.security.CurrentUser;
import com.agritwin.farmtwin.service.CropHistoryService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/v1/farm-twins/me/land-parcels/{parcelId}/crop-history")
@Tag(name = "Crop History", description = "Historical crop cycles logged against a land parcel")
public class CropHistoryController {

    private final CropHistoryService cropHistoryService;
    private final CurrentUser currentUser;

    public CropHistoryController(CropHistoryService cropHistoryService, CurrentUser currentUser) {
        this.cropHistoryService = cropHistoryService;
        this.currentUser = currentUser;
    }

    @PostMapping
    @Operation(summary = "Log a crop history record against a land parcel")
    public ResponseEntity<CropHistoryResponse> create(
            @PathVariable UUID parcelId, @Valid @RequestBody CropHistoryRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED).body(
                cropHistoryService.create(currentUser.id(), parcelId, request));
    }

    @GetMapping
    @Operation(summary = "List crop history records for a land parcel")
    public ResponseEntity<List<CropHistoryResponse>> list(@PathVariable UUID parcelId) {
        return ResponseEntity.ok(cropHistoryService.listForParcel(currentUser.id(), parcelId));
    }

    @GetMapping("/{cropHistoryId}")
    @Operation(summary = "Get a single crop history record")
    public ResponseEntity<CropHistoryResponse> getOne(
            @PathVariable UUID parcelId, @PathVariable UUID cropHistoryId) {
        return ResponseEntity.ok(cropHistoryService.getOneForParcel(currentUser.id(), parcelId, cropHistoryId));
    }
}

package com.agritwin.farmtwin.controller;

import com.agritwin.farmtwin.dto.LandParcelRequest;
import com.agritwin.farmtwin.dto.LandParcelResponse;
import com.agritwin.farmtwin.security.CurrentUser;
import com.agritwin.farmtwin.service.LandParcelService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/v1/farm-twins/me/land-parcels")
@Tag(name = "Land Parcels", description = "Land parcels belonging to the authenticated user's farm twin")
public class LandParcelController {

    private final LandParcelService landParcelService;
    private final CurrentUser currentUser;

    public LandParcelController(LandParcelService landParcelService, CurrentUser currentUser) {
        this.landParcelService = landParcelService;
        this.currentUser = currentUser;
    }

    @PostMapping
    @Operation(summary = "Add a land parcel to the authenticated user's farm twin")
    public ResponseEntity<LandParcelResponse> create(@Valid @RequestBody LandParcelRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED).body(
                landParcelService.create(currentUser.id(), request));
    }

    @GetMapping
    @Operation(summary = "List all land parcels for the authenticated user")
    public ResponseEntity<List<LandParcelResponse>> list() {
        return ResponseEntity.ok(landParcelService.listForCaller(currentUser.id()));
    }

    @GetMapping("/{parcelId}")
    @Operation(summary = "Get a single land parcel by ID")
    public ResponseEntity<LandParcelResponse> getOne(@PathVariable("parcelId") UUID parcelId) {
        return ResponseEntity.ok(landParcelService.getOneForCaller(currentUser.id(), parcelId));
    }
}

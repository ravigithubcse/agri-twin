package com.agritwin.farmtwin.controller;

import com.agritwin.farmtwin.dto.FarmTwinResponse;
import com.agritwin.farmtwin.security.CurrentUser;
import com.agritwin.farmtwin.service.FarmTwinService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1/farm-twins")
@Tag(name = "Farm Twin", description = "Farm Commodity Digital Twin (FCDT) lifecycle")
public class FarmTwinController {

    private final FarmTwinService farmTwinService;
    private final CurrentUser currentUser;

    public FarmTwinController(FarmTwinService farmTwinService, CurrentUser currentUser) {
        this.farmTwinService = farmTwinService;
        this.currentUser = currentUser;
    }

    @PostMapping("/me")
    @Operation(summary = "Create the authenticated user's farm twin (one per user)")
    public ResponseEntity<FarmTwinResponse> createMine() {
        return ResponseEntity.status(HttpStatus.CREATED).body(
                farmTwinService.createForUser(currentUser.id()));
    }

    @GetMapping("/me")
    @Operation(summary = "Get the authenticated user's farm twin")
    public ResponseEntity<FarmTwinResponse> getMine() {
        return ResponseEntity.ok(farmTwinService.getForUser(currentUser.id()));
    }
}

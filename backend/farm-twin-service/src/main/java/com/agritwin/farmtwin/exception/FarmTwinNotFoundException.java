package com.agritwin.farmtwin.exception;

import java.util.UUID;

public class FarmTwinNotFoundException extends RuntimeException {
    public FarmTwinNotFoundException(UUID userId) {
        super("No farm twin exists for user: " + userId);
    }
}

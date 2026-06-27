package com.agritwin.farmtwin.exception;

import java.util.UUID;

public class CropHistoryNotFoundException extends RuntimeException {
    public CropHistoryNotFoundException(UUID id) {
        super("Crop history record not found: " + id);
    }
}

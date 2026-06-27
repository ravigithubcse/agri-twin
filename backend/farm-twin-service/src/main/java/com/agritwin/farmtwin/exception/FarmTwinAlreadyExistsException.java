package com.agritwin.farmtwin.exception;

import java.util.UUID;

public class FarmTwinAlreadyExistsException extends RuntimeException {
    public FarmTwinAlreadyExistsException(UUID userId) {
        super("A farm twin already exists for user: " + userId);
    }
}

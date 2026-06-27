package com.agritwin.farmtwin.exception;

import java.util.UUID;

public class LandParcelNotFoundException extends RuntimeException {
    public LandParcelNotFoundException(UUID id) {
        super("Land parcel not found: " + id);
    }
}

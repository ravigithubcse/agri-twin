package com.agritwin.farmtwin.exception;

/**
 * Thrown when an authenticated user attempts to access a resource
 * (farm twin, land parcel, crop history) that belongs to a different
 * user_id. This is the runtime enforcement of blueprint 5.5's
 * "Resource-level permissions enforced per user_id in every query."
 */
public class AccessDeniedToResourceException extends RuntimeException {
    public AccessDeniedToResourceException() {
        super("You do not have permission to access this resource");
    }
}

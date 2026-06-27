package com.agritwin.user.exception;

public class InvalidRefreshTokenException extends RuntimeException {
    public InvalidRefreshTokenException(String reason) {
        super("Refresh token is invalid: " + reason);
    }
}

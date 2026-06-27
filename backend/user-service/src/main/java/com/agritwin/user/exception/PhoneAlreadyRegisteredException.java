package com.agritwin.user.exception;

public class PhoneAlreadyRegisteredException extends RuntimeException {
    public PhoneAlreadyRegisteredException(String phone) {
        super("An account with phone number " + phone + " already exists");
    }
}

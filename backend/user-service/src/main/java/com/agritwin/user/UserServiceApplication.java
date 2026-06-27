package com.agritwin.user;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

/**
 * AGRI-TWIN AI - User Service
 *
 * Owns: registration, authentication (JWT), RBAC authorisation, and user
 * profile data. This is the identity boundary for the whole platform —
 * every other microservice trusts the JWT this service issues rather than
 * re-implementing auth.
 */
@SpringBootApplication
public class UserServiceApplication {

    public static void main(String[] args) {
        SpringApplication.run(UserServiceApplication.class, args);
    }
}

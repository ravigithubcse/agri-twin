package com.agritwin.farmtwin.security;

import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;

import java.util.UUID;

/**
 * Small convenience wrapper so controllers don't repeat
 * SecurityContextHolder boilerplate. The principal is set to the user's
 * UUID by JwtAuthenticationFilter.
 */
@Component
public class CurrentUser {

    public UUID id() {
        return (UUID) SecurityContextHolder.getContext().getAuthentication().getPrincipal();
    }
}

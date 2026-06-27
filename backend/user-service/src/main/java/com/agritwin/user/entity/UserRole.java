package com.agritwin.user.entity;

/**
 * Maps to blueprint personas (3.2): Persona A/B are FARMER, Persona C is
 * COOPERATIVE_ADMIN, Persona D is ENTERPRISE_BUYER. PLATFORM_ADMIN is the
 * internal ops role from the Admin Panel spec (7.6).
 */
public enum UserRole {
    FARMER,
    COOPERATIVE_ADMIN,
    ENTERPRISE_BUYER,
    PLATFORM_ADMIN
}

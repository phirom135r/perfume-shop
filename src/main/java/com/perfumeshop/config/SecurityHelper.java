package com.perfumeshop.config;

import org.springframework.security.core.Authentication;
import org.springframework.security.oauth2.core.user.OAuth2User;

public final class SecurityHelper {

    private SecurityHelper() {
    }

    public static String currentCustomerEmail(Authentication authentication) {
        if (authentication == null || !authentication.isAuthenticated()) {
            return null;
        }

        Object principal = authentication.getPrincipal();

        if (principal instanceof OAuth2User oauth2User) {
            Object email = oauth2User.getAttributes().get("email");
            if (email != null) {
                return String.valueOf(email).trim().toLowerCase();
            }
        }

        String name = authentication.getName();
        if (name == null || name.isBlank()) {
            return null;
        }

        return name.trim().toLowerCase();
    }
}
package com.dfbs.app.config;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;

/**
 * Sets current user from X-User-Id header (sent by frontend with logged-in user id) so PERM allowlist and other guards see the real user.
 * Runs before controllers; clears thread-local in finally so no leak.
 */
@Component
@Order(-1000)
public class CurrentUserFilter extends OncePerRequestFilter {

    private static final String HEADER_X_USER_ID = "X-User-Id";

    private final CurrentUserProvider currentUserProvider;

    public CurrentUserFilter(CurrentUserProvider currentUserProvider) {
        this.currentUserProvider = currentUserProvider;
    }

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain)
            throws ServletException, IOException {
        try {
            String userId = request.getHeader(HEADER_X_USER_ID);
            if (userId != null && !userId.isBlank()) {
                currentUserProvider.setRequestUser(userId.trim());
            }
            filterChain.doFilter(request, response);
        } finally {
            currentUserProvider.clearRequestUser();
        }
    }
}

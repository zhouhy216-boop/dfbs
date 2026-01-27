package com.dfbs.app.config;

import org.springframework.stereotype.Component;

/**
 * Provides user information by user ID.
 * MVP: Returns placeholder data. In production, this would query a User service/table.
 */
@Component
public class UserInfoProvider {

    public UserInfo getUserInfo(Long userId) {
        if (userId == null) {
            return null;
        }
        // MVP: Return placeholder data
        return new UserInfo(
                "Service Manager " + userId,
                "1380000" + String.format("%04d", userId % 10000),
                "办事处" + userId
        );
    }

    public record UserInfo(String name, String phone, String office) {}
}

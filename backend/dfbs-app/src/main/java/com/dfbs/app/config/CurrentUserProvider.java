package com.dfbs.app.config;

import org.springframework.stereotype.Component;

@Component
public class CurrentUserProvider {

    public String getCurrentUser() {
        return "system";
    }
}

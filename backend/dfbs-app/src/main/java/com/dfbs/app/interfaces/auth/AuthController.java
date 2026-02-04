package com.dfbs.app.interfaces.auth;

import com.dfbs.app.modules.user.UserEntity;
import com.dfbs.app.modules.user.UserRepo;
import lombok.Data;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.server.ResponseStatusException;

import java.util.ArrayList;
import java.util.List;
import java.util.regex.Pattern;

/**
 * Minimal auth controller for MVP login flow.
 * TODO: Replace with real token-based authentication (JWT, OAuth2, etc.)
 */
@RestController
@RequestMapping("/api/auth")
public class AuthController {

    private final UserRepo userRepo;

    public AuthController(UserRepo userRepo) {
        this.userRepo = userRepo;
    }

    @PostMapping("/login")
    public LoginResponse login(@RequestBody LoginRequest request) {
        if (request.getUsername() == null || request.getUsername().isBlank()) {
            throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, "Username required");
        }
        UserEntity user = userRepo.findByUsername(request.getUsername().trim())
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.UNAUTHORIZED, "User not found"));
        List<String> roles = parseAuthorities(user.getAuthorities());
        return new LoginResponse(
                "mock-jwt-token-" + System.currentTimeMillis(),
                new UserInfo(user.getId(), user.getUsername(), user.getNickname(), roles)
        );
    }

    private static List<String> parseAuthorities(String authorities) {
        if (authorities == null || authorities.isBlank()) {
            return List.of("USER");
        }
        String s = authorities.trim();
        List<String> roles = new ArrayList<>();
        if (s.startsWith("[")) {
            Pattern p = Pattern.compile("\"([^\"]+)\"");
            var m = p.matcher(s);
            while (m.find()) {
                String role = m.group(1);
                roles.add(role.startsWith("ROLE_") ? role.substring(5) : role);
            }
        } else {
            for (String part : s.split("[,;]")) {
                String role = part.trim();
                if (!role.isEmpty()) {
                    roles.add(role.startsWith("ROLE_") ? role.substring(5) : role);
                }
            }
        }
        return roles.isEmpty() ? List.of("USER") : roles;
    }

    @Data
    public static class LoginRequest {
        private String username;
        private String password;
    }

    @Data
    public static class LoginResponse {
        private final String token;
        private final UserInfo user;
    }

    /** Login response user; constructor must match (id, username, nickname, roles). */
    @Data
    public static class UserInfo {
        private final Long id;
        private final String username;
        private final String nickname;
        private final List<String> roles;

        public UserInfo(Long id, String username, String nickname, List<String> roles) {
            this.id = id;
            this.username = username;
            this.nickname = nickname;
            this.roles = roles != null ? roles : List.of();
        }
    }
}

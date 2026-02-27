package com.dfbs.app.interfaces.perm;

import com.dfbs.app.application.perm.PermEnforcementService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * Current-user permission read. No PERM allowlist; uses same auth as other APIs (X-User-Id).
 * GET /api/v1/perm/me/effective-keys → { "effectiveKeys": string[] } for frontend RBAC (menu/button hiding).
 */
@RestController
@RequestMapping("/api/v1/perm")
public class PermMeController {

    private final PermEnforcementService permEnforcementService;

    public PermMeController(PermEnforcementService permEnforcementService) {
        this.permEnforcementService = permEnforcementService;
    }

    @GetMapping("/me/effective-keys")
    public ResponseEntity<Map<String, List<String>>> effectiveKeys() {
        var keys = permEnforcementService.getEffectiveKeysForCurrentUser().stream().sorted().collect(Collectors.toList());
        return ResponseEntity.ok(Map.of("effectiveKeys", keys));
    }
}

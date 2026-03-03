package com.dfbs.app.interfaces.bizperm;

import java.util.List;
import java.util.Map;

/**
 * DTOs for per-account op scope API.
 */
public final class BizPermScopeDto {

    private BizPermScopeDto() {}

    public record ScopesResponse(Long userId, Map<String, String> scopes) {}

    public record ScopeUpdateRequest(String permissionKey, String scope) {}

    public record SetScopesRequest(List<ScopeUpdateRequest> updates) {}
}

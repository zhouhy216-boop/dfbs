package com.dfbs.app.interfaces.bizperm;

import java.util.List;

/**
 * Request/response DTOs for Business Module Catalog API.
 */
public final class BizPermCatalogDto {

    private BizPermCatalogDto() {}

    /** Operation point under a node or in unclassified list. id null for computed (universe-only) unclassified. */
    public record OpPoint(Long id, String permissionKey, String cnName, int sortOrder, boolean handledOnly) {}

    /** Tree node with optional children and ops. */
    public record CatalogNode(long id, String cnName, int sortOrder, List<CatalogNode> children, List<OpPoint> ops) {}

    /** Full catalog response. */
    public record CatalogResponse(List<CatalogNode> tree, List<OpPoint> unclassified) {}

    // --- Request DTOs (02.c) ---

    /** Create node: CN name required; parentId optional; sortOrder default end. */
    public record CreateNodeRequest(String cnName, Long parentId, Integer sortOrder) {}

    /** Update node: rename CN, move parent, set sortOrder. */
    public record NodeUpdateRequest(String cnName, Long parentId, Integer sortOrder) {}

    /** Reorder children within same parent. */
    public record ReorderChildrenRequest(List<Long> orderedIds) {}

    /** Create or update op point by permissionKey; nodeId null => unclassified. */
    public record OpPointUpsertRequest(String permissionKey, String cnName, Integer sortOrder, Boolean handledOnly, Long nodeId) {}

    /** Claim op points: move by permissionKeys into nodeId. */
    public record ClaimOpPointsRequest(Long nodeId, List<String> permissionKeys) {}

    /** Update op point (all optional). */
    public record OpPointUpdateRequest(String cnName, Integer sortOrder, Boolean handledOnly, Long nodeId) {}

    /** Toggle handled-only flag. */
    public record UpdateHandledOnlyRequest(boolean handledOnly) {}
}

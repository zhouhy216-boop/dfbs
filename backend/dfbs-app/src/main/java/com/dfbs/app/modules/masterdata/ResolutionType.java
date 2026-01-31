package com.dfbs.app.modules.masterdata;

/** How to resolve a BOM conflict. */
public enum ResolutionType {
    /** Ignore BOM name, keep master SparePart name. */
    KEEP_MASTER,
    /** Overwrite SparePart.name with BOM row name. */
    OVERWRITE_MASTER,
    /** Add BOM row name to SparePart.aliases. */
    ADD_ALIAS,
    /** For MISSING_NO: set item PartNo in BOM JSON to customValue and re-check. */
    FIX_NO
}

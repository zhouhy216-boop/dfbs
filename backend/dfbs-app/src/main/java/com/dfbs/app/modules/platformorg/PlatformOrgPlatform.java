package com.dfbs.app.modules.platformorg;

/**
 * Legacy platform identifiers. Platform is now dynamic (String) from md_platform.
 * @deprecated Use platform code string and {@link com.dfbs.app.application.platformconfig.PlatformConfigService#getRulesByCode(String)} for rules.
 */
@Deprecated
public enum PlatformOrgPlatform {
    INHAND,
    HENDONG,
    JINGPIN,
    OTHER
}

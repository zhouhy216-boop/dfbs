package com.dfbs.app.application.smartselect.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * One item in Smart Select search result (Rule 2.4: is_temp = false only in search).
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class SmartSelectItemDto {
    private Long id;
    private String displayName;
    private String uniqueKey;
    private Boolean isTemp;
}

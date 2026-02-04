package com.dfbs.app.application.smartselect.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class TempPoolItemDto {
    private Long id;
    private String entityType;
    private String uniqueKey;
    private String displayName;
}

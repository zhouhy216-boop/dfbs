package com.dfbs.app.application.smartselect.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class GetOrCreateTempResult {
    private Long id;
    private Boolean isNew;   // true if just created as temp
    private Boolean isTemp;
    private String displayName;
}

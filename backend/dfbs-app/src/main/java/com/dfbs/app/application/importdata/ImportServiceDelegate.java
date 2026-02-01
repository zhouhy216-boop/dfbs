package com.dfbs.app.application.importdata;

import com.dfbs.app.application.importdata.dto.ImportResultDto;

import java.io.InputStream;

/** Common interface for all import services (Parse -> Validate -> Conflict -> Result). */
public interface ImportServiceDelegate {

    ImportResultDto importFromExcel(InputStream file);
}

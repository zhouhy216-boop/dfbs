package com.dfbs.app.interfaces.orgstructure;

import com.dfbs.app.application.orgstructure.OrgChangeLogService;
import com.dfbs.app.config.SuperAdminGuard;
import com.dfbs.app.modules.orgstructure.OrgChangeLogEntity;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import java.time.Instant;
import java.util.Collections;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

/**
 * Regression: change-log list must not crash when date filters are omitted (Postgres
 * "could not determine data type of parameter" when nulls are used in comparisons).
 * Dynamic criteria in service ensures from/to are only applied when non-null.
 */
@WebMvcTest(OrgChangeLogController.class)
class OrgChangeLogControllerTest {

    @Autowired
    private MockMvc mvc;

    @MockBean
    private OrgChangeLogService changeLogService;

    @MockBean
    private SuperAdminGuard superAdminGuard;

    private static PageImpl<OrgChangeLogEntity> emptyPage() {
        return new PageImpl<>(Collections.emptyList(), PageRequest.of(0, 20), 0);
    }

    /** Case A: no filters (objectType/objectId/operatorId/from/to all omitted) → 200 + JSON page */
    @Test
    void list_noFilters_returns200AndJsonPage() throws Exception {
        when(changeLogService.list(null, null, null, null, null, any())).thenReturn(emptyPage());

        mvc.perform(get("/api/v1/org-structure/change-logs")
                        .param("page", "0")
                        .param("size", "20")
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(content().contentTypeCompatibleWith(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.content").isArray())
                .andExpect(jsonPath("$.totalElements").value(0));

        verify(changeLogService).list(null, null, null, null, null, any());
    }

    /** Case B: only from provided → 200 */
    @Test
    void list_onlyFrom_returns200() throws Exception {
        when(changeLogService.list(null, null, null, any(Instant.class), null, any())).thenReturn(emptyPage());

        mvc.perform(get("/api/v1/org-structure/change-logs")
                        .param("from", "2025-01-01")
                        .param("page", "0")
                        .param("size", "20")
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content").isArray());

        verify(changeLogService).list(eq(null), eq(null), eq(null), any(Instant.class), eq(null), any());
    }

    /** Case C: only to provided → 200 */
    @Test
    void list_onlyTo_returns200() throws Exception {
        when(changeLogService.list(null, null, null, null, any(Instant.class), any())).thenReturn(emptyPage());

        mvc.perform(get("/api/v1/org-structure/change-logs")
                        .param("to", "2025-01-31")
                        .param("page", "0")
                        .param("size", "20")
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content").isArray());

        verify(changeLogService).list(eq(null), eq(null), eq(null), eq(null), any(Instant.class), any());
    }

    /** Case D: both from and to provided → 200 */
    @Test
    void list_fromAndTo_returns200() throws Exception {
        when(changeLogService.list(null, null, null, any(Instant.class), any(Instant.class), any())).thenReturn(emptyPage());

        mvc.perform(get("/api/v1/org-structure/change-logs")
                        .param("from", "2025-01-01")
                        .param("to", "2025-01-31")
                        .param("page", "0")
                        .param("size", "20")
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content").isArray());

        verify(changeLogService).list(eq(null), eq(null), eq(null), any(Instant.class), any(Instant.class), any());
    }

    /** Optional: with objectType filter → 200 */
    @Test
    void list_withObjectType_returns200() throws Exception {
        when(changeLogService.list(eq("ORG_NODE"), eq(null), eq(null), eq(null), eq(null), any())).thenReturn(emptyPage());

        mvc.perform(get("/api/v1/org-structure/change-logs")
                        .param("objectType", "ORG_NODE")
                        .param("page", "0")
                        .param("size", "20")
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk());

        verify(changeLogService).list(eq("ORG_NODE"), eq(null), eq(null), eq(null), eq(null), any());
    }

    /** Optional: with objectId (e.g. org node) filter → 200 */
    @Test
    void list_withObjectId_returns200() throws Exception {
        when(changeLogService.list(eq("ORG_NODE"), eq(5L), eq(null), eq(null), eq(null), any())).thenReturn(emptyPage());

        mvc.perform(get("/api/v1/org-structure/change-logs")
                        .param("objectType", "ORG_NODE")
                        .param("objectId", "5")
                        .param("page", "0")
                        .param("size", "20")
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk());

        verify(changeLogService).list(eq("ORG_NODE"), eq(5L), eq(null), eq(null), eq(null), any());
    }
}

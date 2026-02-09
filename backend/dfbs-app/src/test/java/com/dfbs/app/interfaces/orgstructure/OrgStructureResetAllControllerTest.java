package com.dfbs.app.interfaces.orgstructure;

import com.dfbs.app.application.orgstructure.OrgStructureDevResetService;
import com.dfbs.app.config.SuperAdminGuard;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import java.util.Map;

import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

/**
 * MockMvc: POST /api/v1/org-structure/reset-all with confirmText=RESET returns 200 and summary.
 */
@WebMvcTest(OrgStructureDevController.class)
@AutoConfigureMockMvc(addFilters = false)
class OrgStructureResetAllControllerTest {

    @Autowired
    private MockMvc mvc;

    @MockBean
    private OrgStructureDevResetService devResetService;

    @MockBean
    private SuperAdminGuard superAdminGuard;

    @Test
    void postResetAll_withConfirmTextReset_returns200AndSummary() throws Exception {
        when(devResetService.resetAll()).thenReturn(Map.of(
                "positionBindingCleared", 0L,
                "affiliationCleared", 0L,
                "nodeCleared", 0L,
                "levelsRestored", 6,
                "rootNodeCreated", true
        ));

        mvc.perform(post("/api/v1/org-structure/reset-all")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"confirmText\": \"RESET\"}"))
                .andExpect(status().isOk())
                .andExpect(content().contentTypeCompatibleWith(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.levelsRestored").value(6))
                .andExpect(jsonPath("$.rootNodeCreated").value(true));
    }

    @Test
    void postResetAll_withoutConfirmText_returns400() throws Exception {
        mvc.perform(post("/api/v1/org-structure/reset-all")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{}"))
                .andExpect(status().isBadRequest());
    }
}

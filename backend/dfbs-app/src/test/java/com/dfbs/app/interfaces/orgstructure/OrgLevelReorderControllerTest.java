package com.dfbs.app.interfaces.orgstructure;

import com.dfbs.app.application.orgstructure.OrgLevelService;
import com.dfbs.app.config.SuperAdminGuard;
import com.dfbs.app.modules.orgstructure.OrgLevelEntity;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.web.server.ResponseStatusException;

import java.util.List;

import static org.mockito.ArgumentMatchers.anyList;
import static org.mockito.Mockito.*;
import static org.springframework.http.HttpStatus.BAD_REQUEST;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

/**
 * Regression: PUT /api/v1/org-structure/levels/reorder must be reachable (no 404)
 * and return 200 for valid payload, 400 for duplicates / company id / unknown id.
 */
@WebMvcTest(OrgLevelController.class)
@AutoConfigureMockMvc(addFilters = false)
class OrgLevelReorderControllerTest {

    @Autowired
    private MockMvc mvc;

    @MockBean
    private OrgLevelService orgLevelService;

    @MockBean
    private SuperAdminGuard superAdminGuard;

    @Test
    void putReorder_validOrder_returns200() throws Exception {
        OrgLevelEntity a = new OrgLevelEntity();
        a.setId(2L);
        a.setOrderIndex(2);
        a.setDisplayName("本部");
        OrgLevelEntity b = new OrgLevelEntity();
        b.setId(3L);
        b.setOrderIndex(3);
        b.setDisplayName("部");
        when(orgLevelService.reorder(List.of(3L, 2L))).thenReturn(List.of(b, a));

        mvc.perform(put("/api/v1/org-structure/levels/reorder")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"orderedIds\": [3, 2]}"))
                .andExpect(status().isOk())
                .andExpect(content().contentTypeCompatibleWith(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.length()").value(2))
                .andExpect(jsonPath("$[0].id").value(3))
                .andExpect(jsonPath("$[1].id").value(2));

        verify(orgLevelService).reorder(List.of(3L, 2L));
    }

    @Test
    void putReorder_duplicateIds_returns400() throws Exception {
        doThrow(new ResponseStatusException(BAD_REQUEST, "层级ID不能重复"))
                .when(orgLevelService).reorder(anyList());

        mvc.perform(put("/api/v1/org-structure/levels/reorder")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"orderedIds\": [2, 3, 2]}"))
                .andExpect(status().isBadRequest())
                .andExpect(content().string(org.hamcrest.Matchers.containsString("重复")));

        verify(orgLevelService).reorder(List.of(2L, 3L, 2L));
    }

    @Test
    void putReorder_includesCompany_returns400() throws Exception {
        doThrow(new ResponseStatusException(BAD_REQUEST, "不能对系统固定层级「公司」排序"))
                .when(orgLevelService).reorder(anyList());

        mvc.perform(put("/api/v1/org-structure/levels/reorder")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"orderedIds\": [1, 2]}"))
                .andExpect(status().isBadRequest())
                .andExpect(content().string(org.hamcrest.Matchers.containsString("公司")));

        verify(orgLevelService).reorder(List.of(1L, 2L));
    }

    @Test
    void putReorder_unknownId_returns400() throws Exception {
        doThrow(new ResponseStatusException(BAD_REQUEST, "存在无效的层级ID"))
                .when(orgLevelService).reorder(anyList());

        mvc.perform(put("/api/v1/org-structure/levels/reorder")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"orderedIds\": [999]}"))
                .andExpect(status().isBadRequest())
                .andExpect(content().string(org.hamcrest.Matchers.containsString("无效")));

        verify(orgLevelService).reorder(List.of(999L));
    }
}

package com.dfbs.app.interfaces.orgstructure;

import com.dfbs.app.application.orgstructure.OrgNodeService;
import com.dfbs.app.application.orgstructure.dto.OrgNodeDto;
import com.dfbs.app.config.SuperAdminGuard;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import java.time.Instant;

import static org.assertj.core.api.Assertions.assertThat;
import static org.hamcrest.Matchers.not;
import static org.hamcrest.Matchers.containsString;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

/**
 * Ensures GET /nodes/{id} returns a DTO (not JPA entity) so JSON serialization does not hit
 * Hibernate ByteBuddyInterceptor / lazy proxy.
 */
@WebMvcTest(OrgNodeController.class)
class OrgNodeControllerTest {

	@Autowired
	private MockMvc mvc;

	@MockBean
	private OrgNodeService orgNodeService;

	@MockBean
	private SuperAdminGuard superAdminGuard;

	/** Regression: GET nodes/{id} must return 200 and JSON with scalar fields only (no ByteBuddyInterceptor). */
	@Test
	void getNodeById_returns200_andJsonWithScalarFieldsOnly() throws Exception {
		OrgNodeDto dto = new OrgNodeDto(
				1L, 2L, null, "公司", null, true,
				Instant.parse("2025-01-01T00:00:00Z"), Instant.parse("2025-01-02T00:00:00Z"), "admin", "admin"
		);
		when(orgNodeService.getByIdAsDto(1L)).thenReturn(dto);

		mvc.perform(get("/api/v1/org-structure/nodes/1").accept(MediaType.APPLICATION_JSON))
				.andExpect(status().isOk())
				.andExpect(content().contentTypeCompatibleWith(MediaType.APPLICATION_JSON))
				.andExpect(jsonPath("$.id").value(1))
				.andExpect(jsonPath("$.name").value("公司"))
				.andExpect(jsonPath("$.levelId").value(2))
				.andExpect(jsonPath("$.isEnabled").value(true))
				.andExpect(jsonPath("$.createdBy").value("admin"))
				.andExpect(content().string(not(containsString("ByteBuddyInterceptor"))));
	}

	@Test
	void get_returnsDtoWithScalarFields_only() {
        SuperAdminGuard guard = mock(SuperAdminGuard.class);
        OrgNodeService service = mock(OrgNodeService.class);
        OrgNodeDto dto = new OrgNodeDto(
                1L, 2L, null, "公司", null, true,
                Instant.now(), Instant.now(), "admin", "admin"
        );
        when(service.getByIdAsDto(1L)).thenReturn(dto);

        OrgNodeController controller = new OrgNodeController(service, guard);
        OrgNodeDto result = controller.get(1L);

        assertThat(result).isNotNull();
        assertThat(result.id()).isEqualTo(1L);
        assertThat(result.levelId()).isEqualTo(2L);
        assertThat(result.parentId()).isNull();
        assertThat(result.name()).isEqualTo("公司");
        assertThat(result.isEnabled()).isTrue();
        assertThat(result.createdAt()).isNotNull();
        assertThat(result.updatedAt()).isNotNull();
        // No entity/relation fields - safe for JSON
    }

    @Test
    void get_withParentId_returnsDto() {
        SuperAdminGuard guard = mock(SuperAdminGuard.class);
        OrgNodeService service = mock(OrgNodeService.class);
        OrgNodeDto child = new OrgNodeDto(
                10L, 3L, 1L, "营业本部", "备注", true,
                Instant.now(), Instant.now(), "admin", "admin"
        );
        when(service.getByIdAsDto(10L)).thenReturn(child);

        OrgNodeController controller = new OrgNodeController(service, guard);
        OrgNodeDto result = controller.get(10L);

        assertThat(result.id()).isEqualTo(10L);
        assertThat(result.parentId()).isEqualTo(1L);
        assertThat(result.name()).isEqualTo("营业本部");
    }
}

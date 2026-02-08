package com.dfbs.app.interfaces.orgstructure;

import com.dfbs.app.application.orgstructure.OrgLevelService;
import com.dfbs.app.config.SuperAdminGuard;
import org.junit.jupiter.api.Test;

import java.util.HashMap;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

/**
 * Ensures GET /levels/can-reset is not consumed by GET /{id} (can-reset routing).
 */
class OrgLevelControllerTest {

    @Test
    void canReset_returnsJsonWithCanResetKey() {
        SuperAdminGuard guard = mock(SuperAdminGuard.class);
        OrgLevelService service = mock(OrgLevelService.class);
        Map<String, Object> resultMap = new HashMap<>();
        resultMap.put("canReset", true);
        resultMap.put("message", null);
        resultMap.put("nodeCount", 0L);
        resultMap.put("affiliationCount", 0L);
        when(service.getCanResetLevelsResult()).thenReturn(resultMap);

        OrgLevelController controller = new OrgLevelController(service, guard);
        Map<String, Object> result = controller.canReset();

        assertThat(result).containsKey("canReset");
        assertThat(result.get("canReset")).isEqualTo(true);
        assertThat(result).containsKey("message");
        assertThat(result.get("message")).isNull();
        assertThat(result).containsKey("nodeCount");
        assertThat(result).containsKey("affiliationCount");
    }

    @Test
    void canReset_whenNotSafe_returnsCanResetFalseAndMessage() {
        SuperAdminGuard guard = mock(SuperAdminGuard.class);
        OrgLevelService service = mock(OrgLevelService.class);
        when(service.getCanResetLevelsResult()).thenReturn(Map.of(
                "canReset", false,
                "message", "已有组织节点/人员归属使用层级，需先迁移/清理后才能重置。组织节点数 2，人员归属数 0",
                "nodeCount", 2L,
                "affiliationCount", 0L
        ));

        OrgLevelController controller = new OrgLevelController(service, guard);
        Map<String, Object> result = controller.canReset();

        assertThat(result.get("canReset")).isEqualTo(false);
        assertThat(result.get("message")).asString().contains("迁移");
        assertThat(result.get("nodeCount")).isEqualTo(2L);
    }
}

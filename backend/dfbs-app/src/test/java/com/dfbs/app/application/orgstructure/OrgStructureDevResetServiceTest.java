package com.dfbs.app.application.orgstructure;

import com.dfbs.app.modules.orgstructure.*;
import org.junit.jupiter.api.Test;
import org.mockito.InOrder;

import java.util.List;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.*;

/**
 * reset-all: templates deleted before levels so FK fk_olpt_level never blocks.
 */
class OrgStructureDevResetServiceTest {

    @Test
    void resetAll_deletesTemplatesBeforeResettingLevels() {
        OrgChangeLogRepo changeLogRepo = mock(OrgChangeLogRepo.class);
        PersonAffiliationRepo affiliationRepo = mock(PersonAffiliationRepo.class);
        OrgPersonRepo personRepo = mock(OrgPersonRepo.class);
        OrgNodeRepo nodeRepo = mock(OrgNodeRepo.class);
        OrgLevelService levelService = mock(OrgLevelService.class);
        OrgPositionBindingRepo bindingRepo = mock(OrgPositionBindingRepo.class);
        OrgPositionEnabledRepo enabledRepo = mock(OrgPositionEnabledRepo.class);
        OrgLevelPositionTemplateRepo levelPositionTemplateRepo = mock(OrgLevelPositionTemplateRepo.class);
        OrgNodeService nodeService = mock(OrgNodeService.class);

        when(bindingRepo.count()).thenReturn(0L);
        when(enabledRepo.count()).thenReturn(0L);
        when(affiliationRepo.count()).thenReturn(0L);
        when(personRepo.count()).thenReturn(0L);
        when(nodeRepo.count()).thenReturn(0L);
        when(changeLogRepo.count()).thenReturn(0L);
        OrgLevelEntity companyLevel = new OrgLevelEntity();
        companyLevel.setId(1L);
        companyLevel.setDisplayName("公司");
        when(levelService.listOrdered()).thenReturn(List.of(companyLevel));
        when(levelService.resetLevelsToDefault()).thenReturn(List.of(companyLevel));

        OrgStructureDevResetService service = new OrgStructureDevResetService(
                changeLogRepo, affiliationRepo, personRepo, nodeRepo, levelService,
                bindingRepo, enabledRepo, levelPositionTemplateRepo, nodeService);

        Map<String, Object> result = service.resetAll();

        InOrder order = inOrder(levelPositionTemplateRepo, levelService);
        order.verify(levelPositionTemplateRepo).deleteAllInBatch();
        order.verify(levelService).resetLevelsToDefault();

        assertThat(result).containsEntry("levelsRestored", 1);
        assertThat(result).containsEntry("rootNodeCreated", true);
    }
}

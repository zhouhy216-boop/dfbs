package com.dfbs.app.application.orgstructure;

import com.dfbs.app.config.CurrentUserIdResolver;
import com.dfbs.app.modules.orgstructure.*;
import org.junit.jupiter.api.Test;
import org.springframework.web.server.ResponseStatusException;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

class OrgLevelServiceTest {

    @Test
    void update_whenLevelIsCompany_throwsBadRequest() {
        OrgLevelRepo levelRepo = mock(OrgLevelRepo.class);
        OrgNodeRepo nodeRepo = mock(OrgNodeRepo.class);
        PersonAffiliationRepo affiliationRepo = mock(PersonAffiliationRepo.class);
        OrgChangeLogService changeLogService = mock(OrgChangeLogService.class);
        CurrentUserIdResolver userIdResolver = mock(CurrentUserIdResolver.class);
        when(userIdResolver.getCurrentUserId()).thenReturn(1L);
        when(userIdResolver.getCurrentUserEntity()).thenReturn(new com.dfbs.app.modules.user.UserEntity());

        OrgLevelEntity company = new OrgLevelEntity();
        company.setId(1L);
        company.setDisplayName("公司");
        company.setOrderIndex(1);
        company.setIsEnabled(true);
        when(levelRepo.findById(1L)).thenReturn(java.util.Optional.of(company));

        OrgLevelService service = new OrgLevelService(levelRepo, nodeRepo, affiliationRepo, changeLogService, userIdResolver);

        assertThatThrownBy(() -> service.update(1L, 2, "其他", true))
                .isInstanceOf(ResponseStatusException.class)
                .satisfies(ex -> assertThat(((ResponseStatusException) ex).getReason()).contains("公司为系统固定层级"));
    }
}

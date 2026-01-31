package com.dfbs.app.interfaces.statement;

import com.dfbs.app.application.statement.AccountStatementService;
import com.dfbs.app.modules.statement.AccountStatementEntity;
import org.junit.jupiter.api.Test;
import org.springframework.http.HttpStatus;
import org.springframework.web.server.ResponseStatusException;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

/**
 * Unit test for AccountStatementController. Tests permission (403) when user lacks canManageStatements.
 */
class AccountStatementControllerTest {

    @Test
    void generate_whenUserHasNoManagementPermission_throwsResponseStatusException403() {
        AccountStatementService mockService = mock(AccountStatementService.class);
        when(mockService.hasManagementPermission(anyLong())).thenReturn(false);

        AccountStatementController controller = new AccountStatementController(mockService);
        AccountStatementController.GenerateRequest body =
                new AccountStatementController.GenerateRequest(1L, List.of(100L));
        Long userIdNoPerm = 999L;

        assertThatThrownBy(() -> controller.generate(body, userIdNoPerm))
                .isInstanceOf(ResponseStatusException.class)
                .satisfies(ex -> {
                    ResponseStatusException rse = (ResponseStatusException) ex;
                    assertThat(rse.getStatusCode()).isEqualTo(HttpStatus.FORBIDDEN);
                    assertThat(rse.getReason()).isEqualTo("无对账单管理权限");
                });
    }

    @Test
    void generate_whenUserHasManagementPermission_delegatesToService() {
        AccountStatementService mockService = mock(AccountStatementService.class);
        when(mockService.hasManagementPermission(anyLong())).thenReturn(true);
        AccountStatementEntity stub = new AccountStatementEntity();
        stub.setId(1L);
        stub.setStatementNo("ST-20260129-001");
        when(mockService.generate(anyLong(), any(), anyLong())).thenReturn(stub);

        AccountStatementController controller = new AccountStatementController(mockService);
        AccountStatementController.GenerateRequest body =
                new AccountStatementController.GenerateRequest(1L, List.of(100L));
        Long userId = 1L;

        AccountStatementEntity result = controller.generate(body, userId);

        assertThat(result).isNotNull();
        assertThat(result.getId()).isEqualTo(1L);
        assertThat(result.getStatementNo()).isEqualTo("ST-20260129-001");
    }
}

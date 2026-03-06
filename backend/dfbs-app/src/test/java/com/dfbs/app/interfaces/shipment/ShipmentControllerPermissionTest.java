package com.dfbs.app.interfaces.shipment;

import com.dfbs.app.application.perm.PermEnforcementService;
import com.dfbs.app.application.perm.PermForbiddenException;
import com.dfbs.app.application.shipment.ShipmentService;
import com.dfbs.app.modules.shipment.ShipmentEntity;
import com.dfbs.app.modules.shipment.ShipmentStatus;
import org.junit.jupiter.api.Test;
import org.springframework.data.domain.PageImpl;

import java.util.List;
import java.util.Set;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

/**
 * Minimal permission tests: guarded endpoints call requirePermission; missing permission yields PermForbiddenException (403).
 */
class ShipmentControllerPermissionTest {

    @Test
    void accept_whenUserLacksPermission_throwsPermForbiddenException() {
        PermEnforcementService perm = mock(PermEnforcementService.class);
        doThrow(new PermForbiddenException("无权限")).when(perm).requirePermission(ShipmentController.PERM_ACCEPT);
        ShipmentService shipmentService = mock(ShipmentService.class);
        ShipmentController ctrl = new ShipmentController(
                shipmentService,
                mock(com.dfbs.app.application.shipment.ShipmentTypeService.class),
                mock(com.dfbs.app.application.carrier.CarrierService.class),
                perm);

        assertThatThrownBy(() -> ctrl.accept(1L, 1L, null))
                .isInstanceOf(PermForbiddenException.class)
                .hasMessageContaining("无权限");
        verify(perm).requirePermission(ShipmentController.PERM_ACCEPT);
    }

    @Test
    void accept_whenUserHasPermission_delegatesToService() {
        PermEnforcementService perm = mock(PermEnforcementService.class);
        ShipmentService shipmentService = mock(ShipmentService.class);
        ShipmentEntity entity = new ShipmentEntity();
        entity.setId(1L);
        entity.setStatus(ShipmentStatus.PENDING_SHIP);
        when(shipmentService.accept(eq(1L), eq(1L), any())).thenReturn(entity);
        ShipmentController ctrl = new ShipmentController(
                shipmentService,
                mock(com.dfbs.app.application.shipment.ShipmentTypeService.class),
                mock(com.dfbs.app.application.carrier.CarrierService.class),
                perm);

        ShipmentEntity result = ctrl.accept(1L, 1L, null);

        assertThat(result).isNotNull();
        assertThat(result.getStatus()).isEqualTo(ShipmentStatus.PENDING_SHIP);
        verify(perm).requirePermission(ShipmentController.PERM_ACCEPT);
        verify(shipmentService).accept(1L, 1L, null);
    }

    @Test
    void getWorkflow_filtersActionsByEffectiveKeys() {
        PermEnforcementService perm = mock(PermEnforcementService.class);
        when(perm.getEffectiveKeysForCurrentUser()).thenReturn(Set.of(
                ShipmentController.PERM_VIEW,
                ShipmentController.PERM_ACCEPT,
                ShipmentController.PERM_CANCEL
        ));
        ShipmentService shipmentService = mock(ShipmentService.class);
        com.dfbs.app.application.shipment.ShipmentWorkflowDto fullDto = new com.dfbs.app.application.shipment.ShipmentWorkflowDto(
                1L,
                ShipmentStatus.CREATED,
                "REQUEST",
                "申请",
                List.of(
                        new com.dfbs.app.application.shipment.WorkflowActionDto("ACCEPT", "审核并补充", "POST", "/api/v1/shipments/1/accept", null),
                        new com.dfbs.app.application.shipment.WorkflowActionDto("EXCEPTION", "标记异常", "POST", "/api/v1/shipments/1/exception", null),
                        new com.dfbs.app.application.shipment.WorkflowActionDto("CANCEL", "取消", "POST", "/api/v1/shipments/1/cancel", null)
                ));
        when(shipmentService.getWorkflow(1L)).thenReturn(fullDto);
        ShipmentController ctrl = new ShipmentController(
                shipmentService,
                mock(com.dfbs.app.application.shipment.ShipmentTypeService.class),
                mock(com.dfbs.app.application.carrier.CarrierService.class),
                perm);

        com.dfbs.app.application.shipment.ShipmentWorkflowDto result = ctrl.getWorkflow(1L);

        assertThat(result.actions()).hasSize(2);
        assertThat(result.actions().stream().map(a -> a.actionCode())).containsExactlyInAnyOrder("ACCEPT", "CANCEL");
        assertThat(result.actions().stream().map(a -> a.actionCode())).doesNotContain("EXCEPTION");
    }

    @Test
    void close_whenUserLacksPermission_throwsPermForbiddenException() {
        PermEnforcementService perm = mock(PermEnforcementService.class);
        doThrow(new PermForbiddenException("无权限")).when(perm).requirePermission(ShipmentController.PERM_CLOSE);
        ShipmentService shipmentService = mock(ShipmentService.class);
        ShipmentController ctrl = new ShipmentController(
                shipmentService,
                mock(com.dfbs.app.application.shipment.ShipmentTypeService.class),
                mock(com.dfbs.app.application.carrier.CarrierService.class),
                perm);

        assertThatThrownBy(() -> ctrl.close(1L, 1L))
                .isInstanceOf(PermForbiddenException.class)
                .hasMessageContaining("无权限");
        verify(perm).requirePermission(ShipmentController.PERM_CLOSE);
    }

    @Test
    void close_whenUserHasPermission_delegatesToService() {
        PermEnforcementService perm = mock(PermEnforcementService.class);
        ShipmentService shipmentService = mock(ShipmentService.class);
        ShipmentEntity entity = new ShipmentEntity();
        entity.setId(1L);
        entity.setStatus(ShipmentStatus.COMPLETED);
        when(shipmentService.close(eq(1L), eq(1L))).thenReturn(entity);
        ShipmentController ctrl = new ShipmentController(
                shipmentService,
                mock(com.dfbs.app.application.shipment.ShipmentTypeService.class),
                mock(com.dfbs.app.application.carrier.CarrierService.class),
                perm);

        ShipmentEntity result = ctrl.close(1L, 1L);

        assertThat(result).isNotNull();
        assertThat(result.getStatus()).isEqualTo(ShipmentStatus.COMPLETED);
        verify(perm).requirePermission(ShipmentController.PERM_CLOSE);
        verify(shipmentService).close(1L, 1L);
    }

    @Test
    void getExceptions_whenUserLacksView_throwsPermForbiddenException() {
        PermEnforcementService perm = mock(PermEnforcementService.class);
        doThrow(new PermForbiddenException("无权限")).when(perm).requirePermission(ShipmentController.PERM_VIEW);
        ShipmentService shipmentService = mock(ShipmentService.class);
        ShipmentController ctrl = new ShipmentController(
                shipmentService,
                mock(com.dfbs.app.application.shipment.ShipmentTypeService.class),
                mock(com.dfbs.app.application.carrier.CarrierService.class),
                perm);

        assertThatThrownBy(() -> ctrl.getExceptions(1L, null))
                .isInstanceOf(PermForbiddenException.class)
                .hasMessageContaining("无权限");
        verify(perm).requirePermission(ShipmentController.PERM_VIEW);
    }
}

package com.dfbs.app.application.orgstructure;

import com.dfbs.app.config.CurrentUserIdResolver;
import com.dfbs.app.modules.orgstructure.*;
import jakarta.persistence.EntityManager;
import jakarta.persistence.Query;
import org.junit.jupiter.api.Test;
import org.springframework.web.server.ResponseStatusException;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.*;

class OrgLevelServiceTest {

    private static OrgLevelService createService(OrgLevelRepo levelRepo) {
        return createService(levelRepo, null);
    }

    private static OrgLevelService createService(OrgLevelRepo levelRepo, EntityManager entityManager) {
        OrgNodeRepo nodeRepo = mock(OrgNodeRepo.class);
        PersonAffiliationRepo affiliationRepo = mock(PersonAffiliationRepo.class);
        OrgChangeLogService changeLogService = mock(OrgChangeLogService.class);
        CurrentUserIdResolver userIdResolver = mock(CurrentUserIdResolver.class);
        when(userIdResolver.getCurrentUserId()).thenReturn(1L);
        com.dfbs.app.modules.user.UserEntity user = new com.dfbs.app.modules.user.UserEntity();
        when(userIdResolver.getCurrentUserEntity()).thenReturn(user);
        EntityManager em = entityManager != null ? entityManager : mock(EntityManager.class);
        OrgLevelPositionTemplateRepo templateRepo = mock(OrgLevelPositionTemplateRepo.class);
        OrgPositionCatalogRepo catalogRepo = mock(OrgPositionCatalogRepo.class);
        return new OrgLevelService(levelRepo, nodeRepo, affiliationRepo, changeLogService, userIdResolver, em, templateRepo, catalogRepo);
    }

    @Test
    void update_whenLevelIsCompany_throwsBadRequest() {
        OrgLevelRepo levelRepo = mock(OrgLevelRepo.class);
        OrgLevelEntity company = new OrgLevelEntity();
        company.setId(1L);
        company.setDisplayName("公司");
        company.setOrderIndex(1);
        company.setIsEnabled(true);
        when(levelRepo.findById(1L)).thenReturn(Optional.of(company));

        OrgLevelService service = createService(levelRepo);

        assertThatThrownBy(() -> service.update(1L, 2, "其他", true))
                .isInstanceOf(ResponseStatusException.class)
                .satisfies(ex -> assertThat(((ResponseStatusException) ex).getReason()).contains("公司为系统固定层级"));
    }

    @Test
    void create_whenDisplayNameIsCompany_throwsBadRequest() {
        OrgLevelRepo levelRepo = mock(OrgLevelRepo.class);
        when(levelRepo.count()).thenReturn(3L);
        when(levelRepo.findAllByOrderByOrderIndexAsc()).thenReturn(List.of());
        OrgLevelService service = createService(levelRepo);

        assertThatThrownBy(() -> service.create(2, "公司"))
                .isInstanceOf(ResponseStatusException.class)
                .satisfies(ex -> assertThat(((ResponseStatusException) ex).getReason()).contains("不可创建"));
    }

    @Test
    void create_whenOrderIndexOutOfRange_throwsBadRequest() {
        OrgLevelRepo levelRepo = mock(OrgLevelRepo.class);
        when(levelRepo.count()).thenReturn(2L);
        when(levelRepo.findAllByOrderByOrderIndexAsc()).thenReturn(List.of());
        OrgLevelService service = createService(levelRepo);

        assertThatThrownBy(() -> service.create(1, "新层级"))
                .isInstanceOf(ResponseStatusException.class);
        assertThatThrownBy(() -> service.create(9, "新层级"))
                .isInstanceOf(ResponseStatusException.class);
    }

    @Test
    void create_atMiddle_shiftsLowerLevelsAndResultUniqueOrderIndex() {
        OrgLevelRepo levelRepo = mock(OrgLevelRepo.class);
        OrgLevelEntity company = new OrgLevelEntity();
        company.setId(1L);
        company.setDisplayName("公司");
        company.setOrderIndex(1);
        OrgLevelEntity benbu = new OrgLevelEntity();
        benbu.setId(2L);
        benbu.setDisplayName("本部");
        benbu.setOrderIndex(2);
        OrgLevelEntity bu = new OrgLevelEntity();
        bu.setId(3L);
        bu.setDisplayName("部");
        bu.setOrderIndex(3);
        when(levelRepo.count()).thenReturn(3L);
        when(levelRepo.addOrderIndexOffsetWhereGte(2, 1000, "公司")).thenReturn(2);
        when(levelRepo.addOrderIndexOffsetWhereGte(1000, -999, "公司")).thenReturn(2);
        OrgLevelEntity newLevelEntity = newLevel(10L, 2, "新层");
        when(levelRepo.findAllByOrderByOrderIndexAsc())
                .thenReturn(List.of(company, benbu, bu))
                .thenReturn(List.of(company, newLevelEntity, benbuAt(3), buAt(4)));
        when(levelRepo.save(any(OrgLevelEntity.class))).thenAnswer(inv -> {
            OrgLevelEntity e = inv.getArgument(0);
            if (e.getId() == null) return newLevelEntity;
            return e;
        });

        OrgLevelService service = createService(levelRepo);
        OrgLevelEntity created = service.create(2, "新层");

        verify(levelRepo).addOrderIndexOffsetWhereGte(2, 1000, "公司");
        verify(levelRepo).addOrderIndexOffsetWhereGte(1000, -999, "公司");
        assertThat(created.getOrderIndex()).isEqualTo(2);
        assertThat(created.getDisplayName()).isEqualTo("新层");
    }

    private static OrgLevelEntity newLevel(long id, int order, String name) {
        OrgLevelEntity e = new OrgLevelEntity();
        e.setId(id);
        e.setOrderIndex(order);
        e.setDisplayName(name);
        e.setIsEnabled(true);
        return e;
    }

    private static OrgLevelEntity benbuAt(int order) {
        OrgLevelEntity e = new OrgLevelEntity();
        e.setId(2L);
        e.setOrderIndex(order);
        e.setDisplayName("本部");
        return e;
    }

    private static OrgLevelEntity buAt(int order) {
        OrgLevelEntity e = new OrgLevelEntity();
        e.setId(3L);
        e.setOrderIndex(order);
        e.setDisplayName("部");
        return e;
    }

    @Test
    void create_atMiddle_whenAlreadyAtMax_rejected() {
        OrgLevelRepo levelRepo = mock(OrgLevelRepo.class);
        when(levelRepo.count()).thenReturn(8L);
        OrgLevelEntity company = new OrgLevelEntity();
        company.setId(1L);
        company.setDisplayName("公司");
        company.setOrderIndex(1);
        List<OrgLevelEntity> configurable = List.of(
                benbuAt(2), buAt(3), levelAt(4, "课"), levelAt(5, "系"),
                levelAt(6, "班"), levelAt(7, "组"), levelAt(8, "员"));
        List<OrgLevelEntity> all = new ArrayList<>();
        all.add(company);
        all.addAll(configurable);
        when(levelRepo.findAllByOrderByOrderIndexAsc()).thenReturn(all);

        OrgLevelService service = createService(levelRepo);

        assertThatThrownBy(() -> service.create(4, "新层"))
                .isInstanceOf(ResponseStatusException.class)
                .satisfies(ex -> assertThat(((ResponseStatusException) ex).getReason()).contains("8"));
    }

    private static OrgLevelEntity levelAt(int order, String name) {
        OrgLevelEntity e = new OrgLevelEntity();
        e.setId((long) order);
        e.setOrderIndex(order);
        e.setDisplayName(name);
        return e;
    }

    @Test
    void reorder_duplicateIds_throwsBadRequest() {
        OrgLevelRepo levelRepo = mock(OrgLevelRepo.class);
        OrgLevelService service = createService(levelRepo);

        assertThatThrownBy(() -> service.reorder(List.of(2L, 3L, 2L)))
                .isInstanceOf(ResponseStatusException.class)
                .satisfies(ex -> assertThat(((ResponseStatusException) ex).getReason()).contains("重复"));
    }

    @Test
    void reorder_includesCompany_throwsBadRequest() {
        OrgLevelRepo levelRepo = mock(OrgLevelRepo.class);
        OrgLevelEntity company = new OrgLevelEntity();
        company.setId(1L);
        company.setDisplayName("公司");
        company.setOrderIndex(1);
        OrgLevelEntity benbu = new OrgLevelEntity();
        benbu.setId(2L);
        benbu.setDisplayName("本部");
        benbu.setOrderIndex(2);
        when(levelRepo.findAllById(any())).thenReturn(List.of(company, benbu));

        OrgLevelService service = createService(levelRepo);

        assertThatThrownBy(() -> service.reorder(List.of(1L, 2L)))
                .isInstanceOf(ResponseStatusException.class)
                .satisfies(ex -> assertThat(((ResponseStatusException) ex).getReason()).contains("公司"));
    }

    @Test
    void reorder_whenOrderedIdsNotFullSet_throwsBadRequest() {
        OrgLevelRepo levelRepo = mock(OrgLevelRepo.class);
        OrgLevelEntity benbu = new OrgLevelEntity();
        benbu.setId(2L);
        benbu.setDisplayName("本部");
        benbu.setOrderIndex(2);
        OrgLevelEntity bu = new OrgLevelEntity();
        bu.setId(3L);
        bu.setDisplayName("部");
        bu.setOrderIndex(3);
        when(levelRepo.findAllByOrderByOrderIndexAsc()).thenReturn(List.of(benbu, bu));
        when(levelRepo.findAllById(List.of(2L))).thenReturn(List.of(benbu));
        OrgLevelEntity other = new OrgLevelEntity();
        other.setId(999L);
        other.setDisplayName("其他");
        other.setOrderIndex(4);
        when(levelRepo.findAllById(List.of(2L, 3L, 999L))).thenReturn(List.of(benbu, bu, other));

        OrgLevelService service = createService(levelRepo);

        assertThatThrownBy(() -> service.reorder(List.of(2L)))
                .isInstanceOf(ResponseStatusException.class)
                .satisfies(ex -> assertThat(((ResponseStatusException) ex).getReason()).contains("全部可配置层级"));
        assertThatThrownBy(() -> service.reorder(List.of(2L, 3L, 999L)))
                .isInstanceOf(ResponseStatusException.class)
                .satisfies(ex -> assertThat(((ResponseStatusException) ex).getReason()).contains("全部可配置层级"));
    }

    @Test
    void reorder_validOrder_bulkUpdateAndReturnsFreshList() {
        OrgLevelRepo levelRepo = mock(OrgLevelRepo.class);
        OrgLevelEntity benbu = new OrgLevelEntity();
        benbu.setId(2L);
        benbu.setDisplayName("本部");
        benbu.setOrderIndex(2);
        OrgLevelEntity bu = new OrgLevelEntity();
        bu.setId(3L);
        bu.setDisplayName("部");
        bu.setOrderIndex(3);
        OrgLevelEntity buOrder2 = new OrgLevelEntity();
        buOrder2.setId(3L);
        buOrder2.setDisplayName("部");
        buOrder2.setOrderIndex(2);
        OrgLevelEntity benbuOrder3 = new OrgLevelEntity();
        benbuOrder3.setId(2L);
        benbuOrder3.setDisplayName("本部");
        benbuOrder3.setOrderIndex(3);
        when(levelRepo.addOrderIndexOffset(1000, "公司")).thenReturn(2);
        when(levelRepo.findAllByOrderByOrderIndexAsc())
                .thenReturn(List.of(benbu, bu))
                .thenReturn(List.of(buOrder2, benbuOrder3));
        when(levelRepo.findAllById(List.of(3L, 2L))).thenReturn(List.of(bu, benbu));
        EntityManager em = mock(EntityManager.class);
        Query query = mock(Query.class);
        when(em.createNativeQuery(anyString())).thenReturn(query);
        when(query.setParameter(anyString(), any())).thenReturn(query);
        when(query.executeUpdate()).thenReturn(2);

        OrgLevelService service = createService(levelRepo, em);
        List<OrgLevelEntity> result = service.reorder(List.of(3L, 2L));

        verify(levelRepo).addOrderIndexOffset(1000, "公司");
        verify(em).createNativeQuery(anyString());
        verify(query).executeUpdate();
        assertThat(result).hasSize(2);
        assertThat(result.get(0).getOrderIndex()).isEqualTo(2);
        assertThat(result.get(1).getOrderIndex()).isEqualTo(3);
    }
}

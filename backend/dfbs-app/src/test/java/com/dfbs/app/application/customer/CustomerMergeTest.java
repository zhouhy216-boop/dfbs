package com.dfbs.app.application.customer;

import com.dfbs.app.config.ForceFlywayCleanConfig;
import com.dfbs.app.modules.customer.CustomerEntity;
import com.dfbs.app.modules.customer.CustomerRepo;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.Import;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@SpringBootTest
@Transactional
@Import(ForceFlywayCleanConfig.class)
class CustomerMergeTest {

    @Autowired
    private CustomerMasterDataService customerService;

    @Autowired
    private CustomerMergeService mergeService;

    @Autowired
    private CustomerRepo customerRepo;

    @Test
    void uniqueName_duplicateName_fails() {
        String name = "CustA";
        customerService.create("CODE-A1-" + System.currentTimeMillis(), name);

        assertThatThrownBy(() -> customerService.create("CODE-A2-" + System.currentTimeMillis(), name))
                .isInstanceOf(DataIntegrityViolationException.class)
                .hasMessageContaining("客户名称已存在");
    }

    @Test
    void merge_sourceIntoTarget_sourceMerged_targetHasAlias() {
        CustomerEntity a = customerService.create("CODE-M-A-" + System.currentTimeMillis(), "CustA");
        CustomerEntity b = customerService.create("CODE-M-B-" + System.currentTimeMillis(), "CustB");
        Long targetId = a.getId();
        Long sourceId = b.getId();

        var response = mergeService.merge(targetId, sourceId, Map.of(), "test merge", "test-user");

        assertThat(response.mergeLogId()).isNotNull();
        assertThat(response.targetCustomer().getId()).isEqualTo(targetId);

        CustomerEntity sourceAfter = customerRepo.findById(sourceId).orElseThrow();
        assertThat(sourceAfter.getStatus()).isEqualTo("MERGED");
        assertThat(sourceAfter.getMergedToId()).isEqualTo(targetId);
        assertThat(sourceAfter.getName()).startsWith("CustB_MERGED_");

        CustomerEntity targetAfter = customerRepo.findById(targetId).orElseThrow();
        assertThat(targetAfter.getAliases()).anyMatch(al -> "CustB".equals(al.getAliasName()));
    }

    @Test
    void merge_thenSearchBySourceName_returnsTarget() {
        CustomerEntity a = customerService.create("CODE-S-A-" + System.currentTimeMillis(), "CustA");
        CustomerEntity b = customerService.create("CODE-S-B-" + System.currentTimeMillis(), "CustB");
        mergeService.merge(a.getId(), b.getId(), Map.of(), null, "test-user");

        var page = customerService.search("CustB", org.springframework.data.domain.PageRequest.of(0, 10));
        assertThat(page.getContent()).hasSize(1);
        assertThat(page.getContent().get(0).getName()).isEqualTo("CustA");
    }

    @Test
    void undo_afterMerge_restoresBoth() {
        CustomerEntity a = customerService.create("CODE-U-A-" + System.currentTimeMillis(), "CustA");
        CustomerEntity b = customerService.create("CODE-U-B-" + System.currentTimeMillis(), "CustB");
        Long targetId = a.getId();
        Long sourceId = b.getId();

        var response = mergeService.merge(targetId, sourceId, Map.of(), "undo test", "test-user");
        Long logId = response.mergeLogId();

        mergeService.undo(logId);

        CustomerEntity aAfter = customerRepo.findById(targetId).orElseThrow();
        CustomerEntity bAfter = customerRepo.findById(sourceId).orElseThrow();
        assertThat(aAfter.getName()).isEqualTo("CustA");
        assertThat(aAfter.getAliases()).isEmpty();
        assertThat(bAfter.getName()).isEqualTo("CustB");
        assertThat(bAfter.getStatus()).isEqualTo("ACTIVE");
        assertThat(bAfter.getMergedToId()).isNull();
    }

    @Test
    void undoConflict_newCustomerWithSameName_undoFails() {
        CustomerEntity a = customerService.create("CODE-C-A-" + System.currentTimeMillis(), "CustA");
        CustomerEntity b = customerService.create("CODE-C-B-" + System.currentTimeMillis(), "CustB");
        var response = mergeService.merge(a.getId(), b.getId(), Map.of(), null, "test-user");
        Long logId = response.mergeLogId();

        // After merge, B's name is freed (renamed to CustB_MERGED_...). Create new customer with name "CustB"
        customerService.create("CODE-C-NEW-" + System.currentTimeMillis(), "CustB");

        assertThatThrownBy(() -> mergeService.undo(logId))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("Name occupied");
    }
}

package com.dfbs.app.application.repair;

import com.dfbs.app.modules.repair.RepairRecordEntity;
import com.dfbs.app.modules.repair.RepairRecordRepo;
import com.dfbs.app.modules.repair.RepairSource;
import com.dfbs.app.modules.repair.WarrantyStatus;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.data.domain.Page;
import org.springframework.transaction.annotation.Transactional;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.time.LocalDateTime;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest
@Transactional
class RepairRecordTest {

    @Autowired
    private RepairRecordService repairRecordService;

    @Autowired
    private RepairRecordRepo repairRecordRepo;

    private byte[] excelWithRows(RepairRecordExcelRow... rows) {
        ByteArrayOutputStream out = new ByteArrayOutputStream();
        com.alibaba.excel.EasyExcel.write(out, RepairRecordExcelRow.class).sheet("Sheet1").doWrite(List.of(rows));
        return out.toByteArray();
    }

    private RepairRecordExcelRow validRow(String oldNo) {
        RepairRecordExcelRow row = new RepairRecordExcelRow();
        row.setCustomerName("客户A");
        row.setMachineNo("SN001");
        row.setMachineModel("Model-X");
        row.setRepairDateStr("2024-01-15");
        row.setIssueDescription("故障描述");
        row.setResolution("处理结果");
        row.setPersonInCharge("张三");
        row.setWarrantyStatus("在保");
        row.setOldWorkOrderNo(oldNo);
        return row;
    }

    /**
     * Test 1 (Import Success): Mock Excel file with valid data -> Verify DB count = rows.
     */
    @Test
    void test1_importSuccess_validData_verifyDbCount() {
        byte[] excel = excelWithRows(
                validRow("OLD-001"),
                validRow("OLD-002")
        );
        ImportResult result = repairRecordService.importFromExcel(new ByteArrayInputStream(excel), 100L);
        assertThat(result.successCount()).isEqualTo(2);
        assertThat(result.errorMessages()).isEmpty();
        assertThat(repairRecordRepo.count()).isEqualTo(2);
        List<RepairRecordEntity> all = repairRecordRepo.findAll();
        assertThat(all).extracting(RepairRecordEntity::getOldWorkOrderNo).containsExactlyInAnyOrder("OLD-001", "OLD-002");
        assertThat(all).allMatch(e -> e.getSourceType() == RepairSource.HISTORY_IMPORT);
    }

    /**
     * Test 2 (Import Validation): Mock Excel with missing fields -> Verify Error Message list.
     */
    @Test
    void test2_importValidation_missingFields_verifyErrorMessages() {
        RepairRecordExcelRow row = validRow("OLD-003");
        row.setCustomerName(null);
        row.setPersonInCharge("");
        byte[] excel = excelWithRows(row);
        ImportResult result = repairRecordService.importFromExcel(new ByteArrayInputStream(excel), 101L);
        assertThat(result.successCount()).isEqualTo(0);
        assertThat(result.errorMessages()).anyMatch(m -> m.contains("Customer missing"));
        assertThat(result.errorMessages()).anyMatch(m -> m.contains("Person missing"));
    }

    /**
     * Test 3 (Deduplication): Import same file twice -> Second time should fail/skip rows with "Duplicate" error.
     */
    @Test
    void test3_deduplication_importTwice_secondHasDuplicateErrors() {
        byte[] excel = excelWithRows(validRow("OLD-004"), validRow("OLD-005"));
        ImportResult first = repairRecordService.importFromExcel(new ByteArrayInputStream(excel), 102L);
        assertThat(first.successCount()).isEqualTo(2);

        ImportResult second = repairRecordService.importFromExcel(new ByteArrayInputStream(excel), 102L);
        assertThat(second.successCount()).isEqualTo(0);
        assertThat(second.errorMessages()).anyMatch(m -> m.contains("Duplicate Old Work Order No"));
        assertThat(repairRecordRepo.count()).isEqualTo(2);
    }

    /**
     * Test 4 (Search): Insert data -> Search by Customer/SN -> Verify results.
     */
    @Test
    void test4_search_insertThenSearchByCustomerAndSn() {
        byte[] excel = excelWithRows(validRow("OLD-006"));
        repairRecordService.importFromExcel(new ByteArrayInputStream(excel), 103L);

        Page<RepairRecordEntity> byCustomer = repairRecordService.search(
                new RepairRecordFilterRequest("客户A", null, null, null, null, null, null, 0, 10));
        assertThat(byCustomer.getContent()).hasSize(1);
        assertThat(byCustomer.getContent().get(0).getCustomerName()).isEqualTo("客户A");

        Page<RepairRecordEntity> bySn = repairRecordService.search(
                new RepairRecordFilterRequest(null, "SN001", null, null, null, null, null, 0, 10));
        assertThat(bySn.getContent()).hasSize(1);
        assertThat(bySn.getContent().get(0).getMachineNo()).isEqualTo("SN001");

        Page<RepairRecordEntity> byOldNo = repairRecordService.search(
                new RepairRecordFilterRequest(null, null, null, null, null, null, "OLD-006", 0, 10));
        assertThat(byOldNo.getContent()).hasSize(1);
        assertThat(byOldNo.getContent().get(0).getOldWorkOrderNo()).isEqualTo("OLD-006");
    }
}

package com.dfbs.app.application.quote;

import com.dfbs.app.application.quote.QuoteExportService;
import com.dfbs.app.application.quote.QuoteItemService;
import com.dfbs.app.application.quote.QuoteService;
import com.dfbs.app.application.quote.dto.WorkOrderImportRequest;
import com.dfbs.app.config.UserInfoProvider;
import com.dfbs.app.modules.quote.enums.QuoteExpenseType;
import com.dfbs.app.modules.quote.enums.QuoteSourceType;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest
@Transactional
class WorkOrderQuoteTest {

    @Autowired
    private QuoteService quoteService;

    @Autowired
    private QuoteItemService itemService;

    @Autowired
    private QuoteExportService exportService;

    @Autowired
    private UserInfoProvider userInfoProvider;

    @Test
    void scenario1_onsiteFalse_noParts_result1Item() {
        var req = new WorkOrderImportRequest(
                "WO-001",
                1L,
                "Test Customer",
                "Recipient",
                "13800000000",
                "Test Address",
                "Machine Model ABC-123",
                false,
                null,
                100L,
                null  // collectorUserId
        );

        var quote = quoteService.createFromWorkOrder(req);
        var items = itemService.getItems(quote.getId());

        assertThat(items).hasSize(1);
        assertThat(items.get(0).getExpenseType()).isEqualTo(QuoteExpenseType.REPAIR);
        assertThat(quote.getSourceType()).isEqualTo(QuoteSourceType.WORK_ORDER);
        assertThat(quote.getSourceId()).isEqualTo("WO-001");
        assertThat(quote.getAssigneeId()).isEqualTo(100L);
    }

    @Test
    void scenario2_onsiteTrue_noParts_result2Items() {
        var req = new WorkOrderImportRequest(
                "WO-002",
                1L,
                "Test Customer",
                "Recipient",
                "13800000000",
                "Test Address",
                "Machine Model XYZ-456",
                true,
                null,
                200L,
                null  // collectorUserId
        );

        var quote = quoteService.createFromWorkOrder(req);
        var items = itemService.getItems(quote.getId());

        assertThat(items).hasSize(2);
        assertThat(items.get(0).getExpenseType()).isEqualTo(QuoteExpenseType.REPAIR);
        assertThat(items.get(1).getExpenseType()).isEqualTo(QuoteExpenseType.ON_SITE);
    }

    @Test
    void scenario3_onsiteTrue_2Parts_result4Items() {
        var parts = List.of(
                new WorkOrderImportRequest.PartInfo("Part A", 2),
                new WorkOrderImportRequest.PartInfo("Part B", 1)
        );

        var req = new WorkOrderImportRequest(
                "WO-003",
                1L,
                "Test Customer",
                "Recipient",
                "13800000000",
                "Test Address",
                "Machine Model DEF-789",
                true,
                parts,
                300L,
                null  // collectorUserId
        );

        var quote = quoteService.createFromWorkOrder(req);
        var items = itemService.getItems(quote.getId());

        assertThat(items).hasSize(4);
        assertThat(items.get(0).getExpenseType()).isEqualTo(QuoteExpenseType.REPAIR);
        assertThat(items.get(1).getExpenseType()).isEqualTo(QuoteExpenseType.ON_SITE);
        assertThat(items.get(2).getExpenseType()).isEqualTo(QuoteExpenseType.PARTS);
        assertThat(items.get(2).getDescription()).isEqualTo("Part A");
        assertThat(items.get(2).getQuantity()).isEqualTo(2);
        assertThat(items.get(3).getExpenseType()).isEqualTo(QuoteExpenseType.PARTS);
        assertThat(items.get(3).getDescription()).isEqualTo("Part B");
        assertThat(items.get(3).getQuantity()).isEqualTo(1);
    }

    @Test
    void scenario4_verifyHeaderMapping() {
        var req = new WorkOrderImportRequest(
                "WO-004",
                999L,
                "Customer Name",
                "Receiver Name",
                "13900000000",
                "Receiver Address",
                "Machine: Model-123, Serial: SN-456",
                false,
                null,
                500L,
                null  // collectorUserId
        );

        var quote = quoteService.createFromWorkOrder(req);

        assertThat(quote.getSourceType()).isEqualTo(QuoteSourceType.WORK_ORDER);
        assertThat(quote.getSourceId()).isEqualTo("WO-004");
        assertThat(quote.getCustomerId()).isEqualTo(999L);
        assertThat(quote.getRecipient()).isEqualTo("Receiver Name");
        assertThat(quote.getPhone()).isEqualTo("13900000000");
        assertThat(quote.getAddress()).isEqualTo("Receiver Address");
        assertThat(quote.getMachineInfo()).isEqualTo("Machine: Model-123, Serial: SN-456");
        assertThat(quote.getAssigneeId()).isEqualTo(500L);
    }

    @Test
    void scenario5_verifyExportUsesAssigneeInfo() throws Exception {
        var req = new WorkOrderImportRequest(
                "WO-005",
                1L,
                "Test Customer",
                "Recipient",
                "13800000000",
                "Test Address",
                "Machine Info",
                false,
                null,
                123L,
                null  // collectorUserId
        );

        var quote = quoteService.createFromWorkOrder(req);
        var userInfo = userInfoProvider.getUserInfo(123L);

        QuoteExportService.ExportResult result = exportService.export(quote.getId(), "xlsx");

        assertThat(result.bytes()).isNotNull();
        assertThat(result.bytes().length).isGreaterThan(0);
        assertThat(userInfo).isNotNull();
        assertThat(userInfo.name()).contains("Service Manager");
        assertThat(userInfo.phone()).isNotNull();
        assertThat(userInfo.office()).isNotNull();
    }
}


package com.dfbs.app.application.quote.dictionary;

import com.dfbs.app.application.masterdata.PartBomService;
import com.dfbs.app.application.quote.QuoteItemService;
import com.dfbs.app.application.quote.QuoteService;
import com.dfbs.app.application.quote.QuoteValidationException;
import com.dfbs.app.application.quote.dictionary.FeeDictionaryService;
import com.dfbs.app.application.quote.dto.WorkOrderImportRequest;
import com.dfbs.app.modules.masterdata.PartEntity;
import com.dfbs.app.modules.quote.QuoteEntity;
import com.dfbs.app.modules.quote.QuoteItemEntity;
import com.dfbs.app.modules.quote.QuoteItemRepo;
import com.dfbs.app.modules.quote.enums.QuoteExpenseType;
import com.dfbs.app.modules.quote.enums.QuoteSourceType;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@SpringBootTest
@Transactional
class DictionaryQuoteTest {

    @Autowired
    private QuoteService quoteService;

    @Autowired
    private QuoteItemService itemService;

    @Autowired
    private QuoteItemRepo itemRepo;

    @Autowired
    private FeeDictionaryService dictionaryService;

    @Autowired
    private PartBomService partBomService;

    @Test
    void scenario1_draftFlexibility_freeTextSuccess() {
        // Create a quote in DRAFT
        var cmd = new QuoteService.CreateQuoteCommand();
        cmd.setSourceType(QuoteSourceType.MANUAL);
        cmd.setCustomerId(1L);
        QuoteEntity quote = quoteService.createDraft(cmd, "test-user");

        // Create item with free-text (no feeTypeId, no partId) - should succeed in DRAFT
        var itemCmd = new QuoteItemService.CreateItemCommand();
        itemCmd.setExpenseType(QuoteExpenseType.REPAIR);
        itemCmd.setDescription("自由文本维修费");
        itemCmd.setSpec("自定义规格");
        itemCmd.setUnit("次");
        itemCmd.setQuantity(1);
        itemCmd.setUnitPrice(BigDecimal.valueOf(100.00));
        // feeTypeId and partId are null - free text mode

        QuoteItemEntity item = itemService.addItem(quote.getId(), itemCmd);

        assertThat(item).isNotNull();
        assertThat(item.getFeeTypeId()).isNull();
        assertThat(item.getPartId()).isNull();
        assertThat(item.getDescription()).isEqualTo("自由文本维修费");
        assertThat(quote.getStatus().name()).isEqualTo("DRAFT");
    }

    @Test
    void scenario2_confirmBlock_freeTextFails() {
        // Create quote and item with free-text
        var cmd = new QuoteService.CreateQuoteCommand();
        cmd.setSourceType(QuoteSourceType.MANUAL);
        cmd.setCustomerId(1L);
        QuoteEntity quote = quoteService.createDraft(cmd, "test-user");

        var itemCmd = new QuoteItemService.CreateItemCommand();
        itemCmd.setExpenseType(QuoteExpenseType.REPAIR);
        itemCmd.setDescription("自由文本维修费");
        itemCmd.setUnit("次");
        itemCmd.setQuantity(1);
        itemCmd.setUnitPrice(BigDecimal.valueOf(100.00));
        itemService.addItem(quote.getId(), itemCmd);

        // Try to CONFIRM - should fail with QuoteValidationException
        assertThatThrownBy(() -> quoteService.confirm(quote.getId()))
                .isInstanceOf(QuoteValidationException.class)
                .hasMessageContaining("明细第1行：费用类型必须从字典中选择，不能使用自由文本");
    }

    @Test
    void scenario3_confirmSuccess_withValidFeeType() {
        // Create quote
        var cmd = new QuoteService.CreateQuoteCommand();
        cmd.setSourceType(QuoteSourceType.MANUAL);
        cmd.setCustomerId(1L);
        QuoteEntity quote = quoteService.createDraft(cmd, "test-user");

        // Get a valid FeeType from dictionary
        List<com.dfbs.app.modules.quote.dictionary.FeeTypeEntity> activeTypes = dictionaryService.listActiveFeeTypes();
        assertThat(activeTypes).isNotEmpty();
        Long feeTypeId = activeTypes.get(0).getId();

        // Create item with valid feeTypeId
        var itemCmd = new QuoteItemService.CreateItemCommand();
        itemCmd.setExpenseType(QuoteExpenseType.REPAIR);
        itemCmd.setDescription("维修费");
        itemCmd.setUnit("次");
        itemCmd.setQuantity(1);
        itemCmd.setUnitPrice(BigDecimal.valueOf(100.00));
        itemCmd.setFeeTypeId(feeTypeId);
        itemService.addItem(quote.getId(), itemCmd);

        // CONFIRM should succeed
        QuoteEntity confirmed = quoteService.confirm(quote.getId());
        assertThat(confirmed.getStatus().name()).isEqualTo("CONFIRMED");
    }

    @Test
    void scenario4_disableLogic_replacementSaved() {
        // Create two fee types
        Long categoryId = dictionaryService.createCategory("Test Category").getId();
        Long feeType1Id = dictionaryService.createFeeType("Test FeeType 1", categoryId, "次", "次", null).getId();
        Long feeType2Id = dictionaryService.createFeeType("Test FeeType 2", categoryId, "次", "次", null).getId();

        // Disable feeType1 with replacement
        dictionaryService.disableFeeType(feeType1Id, feeType2Id);

        // Verify replacementFeeTypeId is saved
        com.dfbs.app.modules.quote.dictionary.FeeTypeEntity disabled = dictionaryService.getFeeType(feeType1Id);
        assertThat(disabled.getIsActive()).isFalse();
        assertThat(disabled.getReplacementFeeTypeId()).isEqualTo(feeType2Id);
    }

    @Test
    void scenario5_unitAutoFix_onConfirm() {
        // Create quote
        var cmd = new QuoteService.CreateQuoteCommand();
        cmd.setSourceType(QuoteSourceType.MANUAL);
        cmd.setCustomerId(1L);
        QuoteEntity quote = quoteService.createDraft(cmd, "test-user");

        // Get a fee type with allowedUnits
        List<com.dfbs.app.modules.quote.dictionary.FeeTypeEntity> activeTypes = dictionaryService.listActiveFeeTypes();
        com.dfbs.app.modules.quote.dictionary.FeeTypeEntity feeType = activeTypes.stream()
                .filter(ft -> ft.getAllowedUnits() != null && !ft.getAllowedUnits().isBlank())
                .findFirst()
                .orElse(null);

        if (feeType == null) {
            // Create one for testing
            Long categoryId = dictionaryService.createCategory("Test Category").getId();
            feeType = dictionaryService.createFeeType("Test FeeType", categoryId, "次", "次,个", null);
        }

        // Create item with invalid unit (not in allowedUnits)
        var itemCmd = new QuoteItemService.CreateItemCommand();
        itemCmd.setExpenseType(QuoteExpenseType.REPAIR);
        itemCmd.setDescription("测试");
        itemCmd.setUnit("无效单位");  // Invalid unit
        itemCmd.setQuantity(1);
        itemCmd.setUnitPrice(BigDecimal.valueOf(100.00));
        itemCmd.setFeeTypeId(feeType.getId());
        QuoteItemEntity item = itemService.addItem(quote.getId(), itemCmd);

        // Verify invalid unit is set
        assertThat(item.getUnit()).isEqualTo("无效单位");

        // CONFIRM - should auto-fix unit to defaultUnit
        quoteService.confirm(quote.getId());

        // Verify unit was auto-fixed
        QuoteItemEntity updated = itemRepo.findById(item.getId()).orElseThrow();
        assertThat(updated.getUnit()).isEqualTo(feeType.getDefaultUnit());
    }

    @Test
    void scenario6_workOrderIntegration_feeTypeMapped() {
        // Create a part for testing
        PartEntity part = partBomService.createPart("测试配件", "规格A", "个");

        var req = new WorkOrderImportRequest(
                "WO-TEST-001",
                1L,
                "Test Customer",
                "Recipient",
                "13800000000",
                "Test Address",
                "Machine Model TEST",
                true,  // isOnsite
                List.of(new WorkOrderImportRequest.PartInfo("测试配件", 2)),
                100L,
                null  // collectorUserId
        );

        QuoteEntity quote = quoteService.createFromWorkOrder(req);

        // Verify items have correct feeTypeId mapped
        List<QuoteItemEntity> items = itemRepo.findByQuoteIdOrderByLineOrderAsc(quote.getId());

        // Item 1: Repair Fee - should map to "技术服务费"
        QuoteItemEntity repairItem = items.stream()
                .filter(item -> item.getExpenseType() == QuoteExpenseType.REPAIR)
                .findFirst()
                .orElseThrow();
        assertThat(repairItem.getFeeTypeId()).isNotNull();
        com.dfbs.app.modules.quote.dictionary.FeeTypeEntity repairFeeType = dictionaryService.getFeeType(repairItem.getFeeTypeId());
        assertThat(repairFeeType.getName()).isEqualTo("技术服务费");

        // Item 2: Onsite Fee - should map to "登门费"
        QuoteItemEntity onsiteItem = items.stream()
                .filter(item -> item.getExpenseType() == QuoteExpenseType.ON_SITE)
                .findFirst()
                .orElseThrow();
        assertThat(onsiteItem.getFeeTypeId()).isNotNull();
        com.dfbs.app.modules.quote.dictionary.FeeTypeEntity onsiteFeeType = dictionaryService.getFeeType(onsiteItem.getFeeTypeId());
        assertThat(onsiteFeeType.getName()).isEqualTo("登门费");

        // Item 3: Parts Fee - should map to "配件费" and have partId
        QuoteItemEntity partItem = items.stream()
                .filter(item -> item.getExpenseType() == QuoteExpenseType.PARTS)
                .findFirst()
                .orElseThrow();
        assertThat(partItem.getFeeTypeId()).isNotNull();
        com.dfbs.app.modules.quote.dictionary.FeeTypeEntity partFeeType = dictionaryService.getFeeType(partItem.getFeeTypeId());
        assertThat(partFeeType.getName()).isEqualTo("配件费");
        assertThat(partItem.getPartId()).isEqualTo(part.getId());
    }
}

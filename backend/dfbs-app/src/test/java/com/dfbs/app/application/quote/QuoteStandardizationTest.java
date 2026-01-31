package com.dfbs.app.application.quote;

import com.dfbs.app.application.masterdata.PartBomService;
import com.dfbs.app.modules.masterdata.PartEntity;
import com.dfbs.app.modules.quote.QuoteEntity;
import com.dfbs.app.modules.quote.QuoteItemEntity;
import com.dfbs.app.modules.quote.QuoteItemRepo;
import com.dfbs.app.modules.quote.QuoteRepo;
import com.dfbs.app.modules.quote.dictionary.FeeTypeEntity;
import com.dfbs.app.modules.quote.dictionary.FeeTypeRepo;
import com.dfbs.app.modules.quote.enums.QuoteExpenseType;
import com.dfbs.app.modules.quote.enums.QuoteSourceType;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

/**
 * Quote Field Standardization MVP: "Lenient Entry, Strict Exit" for Customer and Parts.
 * Test 1–2: Draft temp customer/part saved. Test 3: Confirm blocked. Test 4–5: Standardize. Test 6: Confirm pass.
 */
@SpringBootTest
@Transactional
class QuoteStandardizationTest {

    @Autowired
    private QuoteService quoteService;

    @Autowired
    private QuoteItemService itemService;

    @Autowired
    private QuoteRepo quoteRepo;

    @Autowired
    private QuoteItemRepo itemRepo;

    @Autowired
    private FeeTypeRepo feeTypeRepo;

    @Autowired
    private PartBomService partBomService;

    private Long standardPartId;

    @BeforeEach
    void ensureTestData() {
        List<FeeTypeEntity> active = feeTypeRepo.findByIsActiveTrue();
        if (active.isEmpty()) {
            throw new IllegalStateException("Test requires at least one active FeeType (seed data)");
        }
        PartEntity part = partBomService.createPart("Standard Part", "Spec", "个");
        part.setSalesPrice(new BigDecimal("50.00"));
        standardPartId = part.getId();
    }

    @Test
    void fullFlow_lenientEntryStrictExit_standardizeThenConfirm() {
        // Test 1: Draft temp customer – create quote with customerName only
        var createCmd = new QuoteService.CreateQuoteCommand();
        createCmd.setSourceType(QuoteSourceType.MANUAL);
        createCmd.setCustomerId(null);
        createCmd.setCustomerName("Temp Client");
        QuoteEntity quote = quoteService.createDraft(createCmd, "test-user");
        Long quoteId = quote.getId();

        assertThat(quote.getId()).isNotNull();
        assertThat(quote.getCustomerId()).isNull();
        assertThat(quote.getCustomerName()).isEqualTo("Temp Client");
        assertThat(quote.getStatus().name()).isEqualTo("DRAFT");

        // Test 2: Draft temp part – add item with description only, no partId
        Long feeTypeId = feeTypeRepo.findByIsActiveTrue().stream().findFirst().orElseThrow().getId();
        var addCmd = new QuoteItemService.CreateItemCommand();
        addCmd.setExpenseType(QuoteExpenseType.PARTS);
        addCmd.setDescription("Temp Part");
        addCmd.setPartId(null);
        addCmd.setFeeTypeId(feeTypeId);
        addCmd.setQuantity(1);
        addCmd.setUnitPrice(BigDecimal.ONE);
        QuoteItemEntity item = itemService.addItem(quoteId, addCmd);
        Long itemId = item.getId();

        assertThat(item.getId()).isNotNull();
        assertThat(item.getPartId()).isNull();
        assertThat(item.getDescription()).isEqualTo("Temp Part");

        // Test 3: Confirm block – expect IllegalStateException (customer not standardized)
        assertThatThrownBy(() -> quoteService.confirm(quoteId))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("客户信息未归一，请先关联标准客户");

        // Test 4: Standardize customer – update header with customerId
        var updateHeaderCmd = new QuoteService.UpdateQuoteCommand();
        updateHeaderCmd.setCustomerId(1L);
        updateHeaderCmd.setCustomerName("Standard Customer");
        quoteService.updateHeader(quoteId, updateHeaderCmd);

        QuoteEntity q = quoteRepo.findById(quoteId).orElseThrow();
        assertThat(q.getOriginalCustomerName()).isEqualTo("Temp Client");
        assertThat(q.getCustomerId()).isEqualTo(1L);
        assertThat(q.getCustomerName()).isEqualTo("Standard Customer");

        // Test 5: Standardize part – update item with partId (keep feeTypeId so confirm still passes)
        var updateItemCmd = new QuoteItemService.UpdateItemCommand();
        updateItemCmd.setFeeTypeId(feeTypeId);
        updateItemCmd.setPartId(standardPartId);
        itemService.updateItem(itemId, updateItemCmd);

        QuoteItemEntity updatedItem = itemRepo.findById(itemId).orElseThrow();
        assertThat(updatedItem.getOriginalPartName()).isEqualTo("Temp Part");
        assertThat(updatedItem.getPartId()).isEqualTo(standardPartId);
        assertThat(updatedItem.getDescription()).isEqualTo("Standard Part");

        // Test 6: Confirm pass – after standardization, confirm succeeds
        QuoteEntity confirmed = quoteService.confirm(quoteId);
        assertThat(confirmed.getStatus().name()).isEqualTo("CONFIRMED");
    }

    @Nested
    class IsolatedCases {

        @Test
        void confirmBlock_partNotStandardized_throwsQuoteValidationException() {
            var createCmd = new QuoteService.CreateQuoteCommand();
            createCmd.setSourceType(QuoteSourceType.MANUAL);
            createCmd.setCustomerId(1L);
            QuoteEntity quote = quoteService.createDraft(createCmd, "test-user");
            Long quoteId = quote.getId();

            Long feeTypeId = feeTypeRepo.findByIsActiveTrue().stream().findFirst().orElseThrow().getId();
            var addCmd = new QuoteItemService.CreateItemCommand();
            addCmd.setExpenseType(QuoteExpenseType.PARTS);
            addCmd.setDescription("Temp Part");
            addCmd.setFeeTypeId(feeTypeId);
            addCmd.setQuantity(1);
            addCmd.setUnitPrice(BigDecimal.ONE);
            itemService.addItem(quoteId, addCmd);

            assertThatThrownBy(() -> quoteService.confirm(quoteId))
                    .isInstanceOf(QuoteValidationException.class)
                    .hasMessageContaining("配件明细未归一，请先关联标准配件");
        }

        @Test
        void createDraft_bothCustomerIdAndCustomerNameNull_throws() {
            var cmd = new QuoteService.CreateQuoteCommand();
            cmd.setSourceType(QuoteSourceType.MANUAL);
            cmd.setCustomerId(null);
            cmd.setCustomerName(null);

            assertThatThrownBy(() -> quoteService.createDraft(cmd, "test-user"))
                    .isInstanceOf(IllegalStateException.class)
                    .hasMessageContaining("客户信息必填");
        }
    }
}

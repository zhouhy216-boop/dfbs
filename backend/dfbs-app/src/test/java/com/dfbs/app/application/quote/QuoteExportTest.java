package com.dfbs.app.application.quote;

import com.dfbs.app.application.quote.QuoteExportService;
import com.dfbs.app.application.quote.QuoteItemService;
import com.dfbs.app.application.quote.QuoteService;
import com.dfbs.app.modules.quote.enums.QuoteExpenseType;
import com.dfbs.app.modules.quote.enums.QuoteItemWarehouse;
import com.dfbs.app.modules.quote.enums.QuoteSourceType;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@SpringBootTest
@Transactional
class QuoteExportTest {

    @Autowired
    private QuoteService quoteService;

    @Autowired
    private QuoteItemService itemService;

    @Autowired
    private QuoteExportService exportService;

    private Long createQuoteWithItems(int itemCount) {
        var createCmd = new QuoteService.CreateQuoteCommand();
        createCmd.setSourceType(QuoteSourceType.MANUAL);
        createCmd.setCustomerId(1L);
        var quote = quoteService.createDraft(createCmd, "test-user");
        Long quoteId = quote.getId();

        for (int i = 0; i < itemCount; i++) {
            var addCmd = new QuoteItemService.CreateItemCommand();
            addCmd.setExpenseType(QuoteExpenseType.PARTS);
            addCmd.setQuantity(i + 1);
            addCmd.setUnitPrice(new BigDecimal("10.00"));
            addCmd.setDescription("Item " + (i + 1));
            addCmd.setWarehouse(i % 2 == 0 ? QuoteItemWarehouse.HEADQUARTERS : QuoteItemWarehouse.LOCAL);
            itemService.addItem(quoteId, addCmd);
        }
        return quoteId;
    }

    @Test
    void exportXlsx_smallItems_noException_nonEmptyStream() throws Exception {
        Long quoteId = createQuoteWithItems(2);
        QuoteExportService.ExportResult result = exportService.export(quoteId, "xlsx");

        assertThat(result.bytes()).isNotNull();
        assertThat(result.bytes().length).isGreaterThan(0);
        assertThat(result.filename()).endsWith(".xlsx");
    }

    @Test
    void exportXlsx_largeItems_dynamicInsert_noError() throws Exception {
        Long quoteId = createQuoteWithItems(12);
        QuoteExportService.ExportResult result = exportService.export(quoteId, "xlsx");

        assertThat(result.bytes()).isNotNull();
        assertThat(result.bytes().length).isGreaterThan(0);
        assertThat(result.filename()).endsWith(".xlsx");
    }

    @Test
    void exportPdf_returnsPdfStream() throws Exception {
        Long quoteId = createQuoteWithItems(2);
        QuoteExportService.ExportResult result = exportService.export(quoteId, "pdf");

        assertThat(result.bytes()).isNotNull();
        assertThat(result.bytes().length).isGreaterThan(0);
        assertThat(result.filename()).endsWith(".pdf");
        String header = new String(result.bytes(), 0, Math.min(5, result.bytes().length), java.nio.charset.StandardCharsets.ISO_8859_1);
        assertThat(header).startsWith("%PDF");
    }

    @Test
    void exportCancelledQuote_throwsOrReturns400() {
        Long quoteId = createQuoteWithItems(2);
        quoteService.cancel(quoteId);

        assertThatThrownBy(() -> exportService.export(quoteId, "xlsx"))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("DRAFT");
    }
}


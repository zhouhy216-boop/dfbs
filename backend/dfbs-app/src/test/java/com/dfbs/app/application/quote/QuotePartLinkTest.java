package com.dfbs.app.application.quote;

import com.dfbs.app.application.bom.BomImportExcelRow;
import com.dfbs.app.application.bom.BomService;
import com.dfbs.app.application.masterdata.PartBomService;
import com.dfbs.app.modules.masterdata.PartEntity;
import com.dfbs.app.modules.masterdata.PartRepo;
import com.dfbs.app.modules.quote.QuoteItemEntity;
import com.dfbs.app.modules.quote.QuoteItemRepo;
import com.dfbs.app.modules.quote.enums.QuoteExpenseType;
import com.dfbs.app.modules.quote.enums.QuoteSourceType;
import com.alibaba.excel.EasyExcel;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.transaction.annotation.Transactional;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.math.BigDecimal;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest
@Transactional
class QuotePartLinkTest {

    @Autowired
    private QuoteService quoteService;

    @Autowired
    private QuoteItemService itemService;

    @Autowired
    private PartBomService partBomService;

    @Autowired
    private BomService bomService;

    @Autowired
    private PartRepo partRepo;

    @Autowired
    private QuoteItemRepo itemRepo;

    /** Test 1 (Search): Search with MachineID returns BOM parts; global search returns all matching. */
    @Test
    void search_withMachineId_returnsBomParts_only() {
        Long machineId = 3000L;
        PartEntity partA = partBomService.createPart("Motor A", "SpecA", "个");
        partA.setSalesPrice(new BigDecimal("100"));
        partRepo.save(partA);
        PartEntity partB = partBomService.createPart("Motor B", "SpecB", "个");
        partRepo.save(partB);
        PartEntity partC = partBomService.createPart("Gear C", "SpecC", "个");
        partRepo.save(partC);

        byte[] bomExcel = bomExcel(
                row("1", "Motor A", "SpecA", null, "1", null, null),
                row("2", "Gear C", "SpecC", null, "1", null, null)
        );
        bomService.importBom(new ByteArrayInputStream(bomExcel), machineId);

        List<PartEntity> withMachine = partBomService.searchWithMachine("Motor", machineId);
        assertThat(withMachine).hasSize(1);
        assertThat(withMachine.get(0).getName()).isEqualTo("Motor A");

        List<PartEntity> global = partBomService.searchWithMachine("Motor", null);
        assertThat(global).hasSize(2).extracting(PartEntity::getName).containsExactlyInAnyOrder("Motor A", "Motor B");
    }

    /** Test 2 (Create Item): Create with partId -> Name/Spec/StandardPrice filled. */
    @Test
    void createItem_withPartId_fillsNameSpecStandardPrice() {
        PartEntity part = partBomService.createPart("Link Part", "Link Spec", "个");
        part.setSalesPrice(new BigDecimal("25.50"));
        partRepo.save(part);

        var createCmd = new QuoteService.CreateQuoteCommand();
        createCmd.setSourceType(QuoteSourceType.MANUAL);
        createCmd.setCustomerId(1L);
        var quote = quoteService.createDraft(createCmd, "test-user");

        var addCmd = new QuoteItemService.CreateItemCommand();
        addCmd.setExpenseType(QuoteExpenseType.PARTS);
        addCmd.setQuantity(1);
        addCmd.setPartId(part.getId());
        addCmd.setUnitPrice(null);

        QuoteItemEntity item = itemService.addItem(quote.getId(), addCmd);

        assertThat(item.getPartId()).isEqualTo(part.getId());
        assertThat(item.getDescription()).isEqualTo("Link Part");
        assertThat(item.getSpec()).isEqualTo("Link Spec");
        assertThat(item.getStandardPrice()).isEqualByComparingTo(new BigDecimal("25.50"));
        assertThat(item.getUnitPrice()).isEqualByComparingTo(new BigDecimal("25.50"));
    }

    /** Test 3 (Price Dev): UnitPrice = Standard -> Dev=False; UnitPrice != Standard -> Dev=True. */
    @Test
    void priceDeviation_unitPriceVsStandard() {
        PartEntity part = partBomService.createPart("Price Part", "Spec", "个");
        part.setSalesPrice(new BigDecimal("100"));
        partRepo.save(part);

        var createCmd = new QuoteService.CreateQuoteCommand();
        createCmd.setSourceType(QuoteSourceType.MANUAL);
        createCmd.setCustomerId(1L);
        var quote = quoteService.createDraft(createCmd, "test-user");

        var addCmd = new QuoteItemService.CreateItemCommand();
        addCmd.setExpenseType(QuoteExpenseType.PARTS);
        addCmd.setQuantity(1);
        addCmd.setPartId(part.getId());
        addCmd.setUnitPrice(new BigDecimal("100"));

        QuoteItemEntity item = itemService.addItem(quote.getId(), addCmd);
        assertThat(item.getIsPriceDeviated()).isFalse();

        var updateCmd = new QuoteItemService.UpdateItemCommand();
        updateCmd.setUnitPrice(new BigDecimal("90"));
        QuoteItemEntity updated = itemService.updateItem(item.getId(), updateCmd);
        assertThat(updated.getIsPriceDeviated()).isTrue();
    }

    /** Test 4 (Snapshot): Update Part Master Price -> existing QuoteItem standardPrice does NOT change. */
    @Test
    void snapshot_quoteItemStandardPrice_unchangedWhenPartMasterUpdated() {
        PartEntity part = partBomService.createPart("Snapshot Part", "Spec", "个");
        part.setSalesPrice(new BigDecimal("50"));
        partRepo.save(part);

        var createCmd = new QuoteService.CreateQuoteCommand();
        createCmd.setSourceType(QuoteSourceType.MANUAL);
        createCmd.setCustomerId(1L);
        var quote = quoteService.createDraft(createCmd, "test-user");

        var addCmd = new QuoteItemService.CreateItemCommand();
        addCmd.setExpenseType(QuoteExpenseType.PARTS);
        addCmd.setQuantity(1);
        addCmd.setPartId(part.getId());
        addCmd.setUnitPrice(new BigDecimal("50"));
        QuoteItemEntity item = itemService.addItem(quote.getId(), addCmd);
        assertThat(item.getStandardPrice()).isEqualByComparingTo(new BigDecimal("50"));

        part.setSalesPrice(new BigDecimal("60"));
        partRepo.save(part);

        QuoteItemEntity reloaded = itemRepo.findById(item.getId()).orElseThrow();
        assertThat(reloaded.getStandardPrice()).isEqualByComparingTo(new BigDecimal("50"));
    }

    private static BomImportExcelRow row(String indexNo, String partName, String spec, String drawingNo,
                                         String qty, String isOpt, String remark) {
        BomImportExcelRow r = new BomImportExcelRow();
        r.setIndexNo(indexNo);
        r.setPartName(partName);
        r.setSpec(spec);
        r.setDrawingNo(drawingNo);
        r.setQuantity(qty);
        r.setIsOptional(isOpt);
        r.setRemark(remark);
        return r;
    }

    private byte[] bomExcel(BomImportExcelRow... rows) {
        ByteArrayOutputStream out = new ByteArrayOutputStream();
        EasyExcel.write(out, BomImportExcelRow.class).sheet("BOM").doWrite(List.of(rows));
        return out.toByteArray();
    }
}

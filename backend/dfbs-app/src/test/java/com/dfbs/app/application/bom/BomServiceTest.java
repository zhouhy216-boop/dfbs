package com.dfbs.app.application.bom;

import com.dfbs.app.application.masterdata.PartBomService;
import com.dfbs.app.application.masterdata.PartImportExcelRow;
import com.dfbs.app.application.masterdata.PartImportResult;
import com.dfbs.app.modules.bom.BomVersionEntity;
import com.dfbs.app.modules.bom.BomVersionRepo;
import com.dfbs.app.modules.masterdata.PartEntity;
import com.dfbs.app.modules.masterdata.PartRepo;
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
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@SpringBootTest
@Transactional
class BomServiceTest {

    @Autowired
    private PartBomService partBomService;

    @Autowired
    private BomService bomService;

    @Autowired
    private PartRepo partRepo;

    @Autowired
    private BomVersionRepo bomVersionRepo;

    private byte[] partExcel(PartImportExcelRow... rows) {
        ByteArrayOutputStream out = new ByteArrayOutputStream();
        EasyExcel.write(out, PartImportExcelRow.class).sheet("Parts").doWrite(List.of(rows));
        return out.toByteArray();
    }

    private byte[] bomExcel(BomImportExcelRow... rows) {
        ByteArrayOutputStream out = new ByteArrayOutputStream();
        EasyExcel.write(out, BomImportExcelRow.class).sheet("BOM").doWrite(List.of(rows));
        return out.toByteArray();
    }

    /** Test 1 (Part Import): Import Excel -> Check SystemNo generated, Price saved. */
    @Test
    void partImport_generatesSystemNoAndSavesPrice() {
        PartImportExcelRow row = new PartImportExcelRow();
        row.setName("Test Part");
        row.setSpec("Spec A");
        row.setPrice("99.50");
        row.setDrawingNo("DRW-001");
        byte[] excel = partExcel(row);
        PartImportResult result = partBomService.importParts(new ByteArrayInputStream(excel));
        assertThat(result.created()).isEqualTo(1);
        assertThat(result.errors()).isEmpty();
        List<PartEntity> parts = partRepo.findAll();
        assertThat(parts).hasSize(1);
        PartEntity p = parts.get(0);
        assertThat(p.getSystemNo()).startsWith("PT-").matches("PT-\\d{8}-\\d{3}");
        assertThat(p.getSalesPrice()).isEqualByComparingTo(new BigDecimal("99.50"));
        assertThat(p.getName()).isEqualTo("Test Part");
        assertThat(p.getDrawingNo()).isEqualTo("DRW-001");
    }

    /** Test 2 (BOM Versioning): Import BOM v1 then v2 for Machine A -> v2 is active, v1 in history. */
    @Test
    void bomVersioning_v2Active_v1InHistory() {
        Long machineId = 1000L;
        PartEntity part = partBomService.createPart("BOM Part", "Spec", "个");
        part.setSalesPrice(new BigDecimal("10"));
        partRepo.save(part);

        BomImportExcelRow row1 = new BomImportExcelRow();
        row1.setIndexNo("1");
        row1.setPartName("BOM Part");
        row1.setSpec("Spec");
        row1.setQuantity("2");
        byte[] excel1 = bomExcel(row1);
        BomVersionEntity v1 = bomService.importBom(new ByteArrayInputStream(excel1), machineId);
        assertThat(v1.getVersion()).isEqualTo(1);
        assertThat(v1.getIsActive()).isTrue();

        BomImportExcelRow row2 = new BomImportExcelRow();
        row2.setIndexNo("1");
        row2.setPartName("BOM Part");
        row2.setSpec("Spec");
        row2.setQuantity("3");
        byte[] excel2 = bomExcel(row2);
        BomVersionEntity v2 = bomService.importBom(new ByteArrayInputStream(excel2), machineId);
        assertThat(v2.getVersion()).isEqualTo(2);
        assertThat(v2.getIsActive()).isTrue();

        assertThat(bomService.getActiveBom(machineId).orElseThrow().getId()).isEqualTo(v2.getId());
        List<BomVersionEntity> history = bomService.getBomHistory(machineId);
        assertThat(history).hasSize(2);
        assertThat(history.get(0).getVersion()).isEqualTo(2);
        assertThat(history.get(1).getVersion()).isEqualTo(1);
        assertThat(bomVersionRepo.findById(v1.getId()).orElseThrow().getIsActive()).isFalse();
    }

    /** Test 3 (Price Deviation): Part Price=100 RMB. Case A: 100 RMB, 1.0 -> false. Case B: 10 USD, 7.0 -> true. */
    @Test
    void priceDeviation_standardVsActual() {
        PartEntity part = partBomService.createPart("Price Part", "Spec", "个");
        part.setSalesPrice(new BigDecimal("100"));
        partRepo.save(part);
        Long partId = part.getId();

        assertThat(partBomService.isPriceDeviated(partId, new BigDecimal("100"), BigDecimal.ONE)).isFalse();
        assertThat(partBomService.isPriceDeviated(partId, new BigDecimal("10"), new BigDecimal("7.0"))).isTrue();
    }

    /** Test 4 (Validation): Import BOM with duplicate IndexNo -> Expect Exception. */
    @Test
    void bomImport_duplicateIndexNo_throwsException() {
        Long machineId = 2000L;
        PartEntity part = partBomService.createPart("Dup Part", "Spec", "个");
        partRepo.save(part);

        BomImportExcelRow row1 = new BomImportExcelRow();
        row1.setIndexNo("1");
        row1.setPartName("Dup Part");
        row1.setSpec("Spec");
        row1.setQuantity("1");
        BomImportExcelRow row2 = new BomImportExcelRow();
        row2.setIndexNo("1");
        row2.setPartName("Dup Part");
        row2.setSpec("Spec");
        row2.setQuantity("2");
        byte[] excel = bomExcel(row1, row2);

        assertThatThrownBy(() -> bomService.importBom(new ByteArrayInputStream(excel), machineId))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("Duplicate IndexNo");
    }
}

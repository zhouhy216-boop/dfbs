package com.dfbs.app.application.quote.dictionary;

import com.dfbs.app.application.masterdata.PartBomService;
import com.dfbs.app.application.quote.dictionary.FeeDictionaryService;
import com.dfbs.app.modules.masterdata.PartEntity;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.transaction.annotation.Transactional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@SpringBootTest
@Transactional
class DictionaryLogicTest {

    @Autowired
    private FeeDictionaryService feeDictionaryService;

    @Autowired
    private PartBomService partBomService;

    @Test
    void disableFeeType_requiresReplacement() {
        // Create two fee types
        Long categoryId = feeDictionaryService.createCategory("Test Category").getId();
        Long feeType1Id = feeDictionaryService.createFeeType("FeeType 1", categoryId, "?", "?", null).getId();
        Long feeType2Id = feeDictionaryService.createFeeType("FeeType 2", categoryId, "?", "?", null).getId();

        // Try to disable without replacement - should fail
        assertThatThrownBy(() -> feeDictionaryService.disableFeeType(feeType1Id, null))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("replacementFeeTypeId is required");

        // Try to disable with self as replacement - should fail
        assertThatThrownBy(() -> feeDictionaryService.disableFeeType(feeType1Id, feeType1Id))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("Cannot replace feeType with itself");

        // Disable with valid replacement - should succeed
        feeDictionaryService.disableFeeType(feeType1Id, feeType2Id);
        
        var disabled = feeDictionaryService.getFeeType(feeType1Id);
        assertThat(disabled.getIsActive()).isFalse();
        assertThat(disabled.getReplacementFeeTypeId()).isEqualTo(feeType2Id);
    }

    @Test
    void disableFeeType_replacementMustBeActive() {
        Long categoryId = feeDictionaryService.createCategory("Test Category").getId();
        Long feeType1Id = feeDictionaryService.createFeeType("FeeType 1", categoryId, "?", "?", null).getId();
        Long feeType2Id = feeDictionaryService.createFeeType("FeeType 2", categoryId, "?", "?", null).getId();
        Long feeType3Id = feeDictionaryService.createFeeType("FeeType 3", categoryId, "?", "?", null).getId();

        // Disable feeType2 first
        feeDictionaryService.disableFeeType(feeType2Id, feeType3Id);

        // Try to use disabled feeType2 as replacement - should fail
        assertThatThrownBy(() -> feeDictionaryService.disableFeeType(feeType1Id, feeType2Id))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("Replacement feeType must be active");
    }

    @Test
    void disableCategory_requiresReplacementAndMovesFeeTypes() {
        // Create categories
        Long category1Id = feeDictionaryService.createCategory("Category 1").getId();
        Long category2Id = feeDictionaryService.createCategory("Category 2").getId();

        // Create fee types in category1
        Long feeType1Id = feeDictionaryService.createFeeType("FeeType 1", category1Id, "?", "?", null).getId();
        Long feeType2Id = feeDictionaryService.createFeeType("FeeType 2", category1Id, "?", "?", null).getId();

        // Try to disable without replacement - should fail
        assertThatThrownBy(() -> feeDictionaryService.disableCategory(category1Id, null))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("replacementCategoryId is required");

        // Try to disable with self as replacement - should fail
        assertThatThrownBy(() -> feeDictionaryService.disableCategory(category1Id, category1Id))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("Cannot replace category with itself");

        // Disable with valid replacement - should move fee types
        feeDictionaryService.disableCategory(category1Id, category2Id);

        var disabled = feeDictionaryService.getCategory(category1Id);
        assertThat(disabled.getIsActive()).isFalse();

        // Verify fee types were moved to category2
        var feeType1 = feeDictionaryService.getFeeType(feeType1Id);
        var feeType2 = feeDictionaryService.getFeeType(feeType2Id);
        assertThat(feeType1.getCategoryId()).isEqualTo(category2Id);
        assertThat(feeType2.getCategoryId()).isEqualTo(category2Id);
    }

    @Test
    void disablePart_requiresReplacement() {
        // Create two parts
        Long part1Id = partBomService.createPart("Part 1", "Spec 1", "?").getId();
        Long part2Id = partBomService.createPart("Part 2", "Spec 2", "?").getId();

        // Try to disable without replacement - should fail
        assertThatThrownBy(() -> partBomService.disablePart(part1Id, null))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("replacementPartId is required");

        // Try to disable with self as replacement - should fail
        assertThatThrownBy(() -> partBomService.disablePart(part1Id, part1Id))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("Cannot replace part with itself");

        // Disable with valid replacement - should succeed
        partBomService.disablePart(part1Id, part2Id);

        var disabled = partBomService.getPart(part1Id);
        assertThat(disabled.getIsActive()).isFalse();
        assertThat(disabled.getReplacementPartId()).isEqualTo(part2Id);
    }

    @Test
    void disablePart_replacementMustBeActive() {
        Long part1Id = partBomService.createPart("Part 1", "Spec 1", "?").getId();
        Long part2Id = partBomService.createPart("Part 2", "Spec 2", "?").getId();
        Long part3Id = partBomService.createPart("Part 3", "Spec 3", "?").getId();

        // Disable part2 first
        partBomService.disablePart(part2Id, part3Id);

        // Try to use disabled part2 as replacement - should fail
        assertThatThrownBy(() -> partBomService.disablePart(part1Id, part2Id))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("Replacement part must be active");
    }
}

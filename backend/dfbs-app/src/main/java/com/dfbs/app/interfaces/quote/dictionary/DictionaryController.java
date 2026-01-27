package com.dfbs.app.interfaces.quote.dictionary;

import com.dfbs.app.application.quote.dictionary.FeeDictionaryService;
import com.dfbs.app.modules.quote.dictionary.FeeCategoryEntity;
import com.dfbs.app.modules.quote.dictionary.FeeTypeEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/v1/dictionary")
public class DictionaryController {

    private final FeeDictionaryService dictionaryService;

    public DictionaryController(FeeDictionaryService dictionaryService) {
        this.dictionaryService = dictionaryService;
    }

    /**
     * List all fee categories.
     */
    @GetMapping("/categories")
    public List<FeeCategoryEntity> listCategories() {
        return dictionaryService.listCategories();
    }

    /**
     * List all fee types (with replacement logic).
     * If a fee type is disabled, it will have replacementFeeTypeId set.
     */
    @GetMapping("/fee-types")
    public List<FeeTypeEntity> listFeeTypes() {
        return dictionaryService.listFeeTypes();
    }

    /**
     * List only active fee types.
     */
    @GetMapping("/fee-types/active")
    public List<FeeTypeEntity> listActiveFeeTypes() {
        return dictionaryService.listActiveFeeTypes();
    }

    /**
     * List all unique units from active fee types.
     * Includes defaultUnit and allowedUnits (parsed from comma-separated values).
     */
    @GetMapping("/units")
    public Set<String> listUnits() {
        List<FeeTypeEntity> activeTypes = dictionaryService.listActiveFeeTypes();
        Set<String> units = activeTypes.stream()
                .filter(ft -> ft.getDefaultUnit() != null && !ft.getDefaultUnit().isBlank())
                .map(FeeTypeEntity::getDefaultUnit)
                .collect(Collectors.toSet());
        
        // Also parse allowedUnits
        activeTypes.stream()
                .filter(ft -> ft.getAllowedUnits() != null && !ft.getAllowedUnits().isBlank())
                .forEach(ft -> {
                    String[] allowed = ft.getAllowedUnits().split(",");
                    for (String unit : allowed) {
                        String trimmed = unit.trim();
                        if (!trimmed.isEmpty()) {
                            units.add(trimmed);
                        }
                    }
                });
        
        return units;
    }
}

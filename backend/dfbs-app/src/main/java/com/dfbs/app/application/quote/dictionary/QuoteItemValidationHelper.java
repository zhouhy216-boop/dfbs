package com.dfbs.app.application.quote.dictionary;

import com.dfbs.app.modules.quote.QuoteItemEntity;
import com.dfbs.app.modules.quote.dictionary.FeeTypeEntity;
import com.dfbs.app.modules.quote.dictionary.FeeTypeRepo;
import com.dfbs.app.modules.quote.enums.QuoteExpenseType;
import com.dfbs.app.modules.masterdata.PartEntity;
import com.dfbs.app.modules.masterdata.PartRepo;
import org.springframework.stereotype.Component;

import java.util.Arrays;
import java.util.List;

/**
 * Validation helper for QuoteItem to ensure it complies with dictionary rules.
 */
@Component
public class QuoteItemValidationHelper {

    private final FeeTypeRepo feeTypeRepo;
    private final PartRepo partRepo;

    public QuoteItemValidationHelper(FeeTypeRepo feeTypeRepo, PartRepo partRepo) {
        this.feeTypeRepo = feeTypeRepo;
        this.partRepo = partRepo;
    }

    /**
     * Validates a QuoteItem against dictionary rules.
     * 
     * @param item The QuoteItem to validate
     * @throws IllegalStateException if validation fails
     */
    public void validateQuoteItem(QuoteItemEntity item) {
        // 1. If feeTypeId is set: Check if Active
        if (item.getFeeTypeId() != null) {
            FeeTypeEntity feeType = feeTypeRepo.findById(item.getFeeTypeId())
                    .orElseThrow(() -> new IllegalStateException("FeeType not found: id=" + item.getFeeTypeId()));
            
            if (!feeType.getIsActive()) {
                throw new IllegalStateException("FeeType is not active: id=" + item.getFeeTypeId());
            }

            // 2. If feeTypeId has allowedUnits: Check if item.unit is allowed
            if (feeType.getAllowedUnits() != null && !feeType.getAllowedUnits().trim().isEmpty()) {
                List<String> allowedUnits = parseCommaSeparated(feeType.getAllowedUnits());
                if (item.getUnit() != null && !allowedUnits.contains(item.getUnit())) {
                    throw new IllegalStateException(
                            String.format("Unit '%s' is not allowed for FeeType '%s'. Allowed units: %s",
                                    item.getUnit(), feeType.getName(), feeType.getAllowedUnits()));
                }
            }

            // 3. If feeTypeId has fixedSpecOptions: Check if item.spec is in options
            if (feeType.getFixedSpecOptions() != null && !feeType.getFixedSpecOptions().trim().isEmpty()) {
                List<String> fixedOptions = parseCommaSeparated(feeType.getFixedSpecOptions());
                if (item.getSpec() != null && !fixedOptions.contains(item.getSpec())) {
                    throw new IllegalStateException(
                            String.format("Spec '%s' is not allowed for FeeType '%s'. Allowed specs: %s",
                                    item.getSpec(), feeType.getName(), feeType.getFixedSpecOptions()));
                }
            }
        }

        // 4. If expenseType == PARTS: Check if partId is set and valid (Active)
        if (item.getExpenseType() == QuoteExpenseType.PARTS) {
            if (item.getPartId() == null) {
                throw new IllegalStateException("partId is required when expenseType is PARTS");
            }
            
            PartEntity part = partRepo.findById(item.getPartId())
                    .orElseThrow(() -> new IllegalStateException("Part not found: id=" + item.getPartId()));
            
            if (!part.getIsActive()) {
                throw new IllegalStateException("Part is not active: id=" + item.getPartId());
            }
        }
    }

    /**
     * Parses a comma-separated string into a list of trimmed strings.
     */
    private List<String> parseCommaSeparated(String value) {
        if (value == null || value.trim().isEmpty()) {
            return List.of();
        }
        return Arrays.stream(value.split(","))
                .map(String::trim)
                .filter(s -> !s.isEmpty())
                .toList();
    }
}

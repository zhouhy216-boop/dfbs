package com.dfbs.app.application.quote.dictionary;

import com.dfbs.app.modules.quote.dictionary.FeeCategoryEntity;
import com.dfbs.app.modules.quote.dictionary.FeeCategoryRepo;
import com.dfbs.app.modules.quote.dictionary.FeeTypeEntity;
import com.dfbs.app.modules.quote.dictionary.FeeTypeRepo;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
public class FeeDictionaryService {

    private final FeeCategoryRepo categoryRepo;
    private final FeeTypeRepo feeTypeRepo;

    public FeeDictionaryService(FeeCategoryRepo categoryRepo, FeeTypeRepo feeTypeRepo) {
        this.categoryRepo = categoryRepo;
        this.feeTypeRepo = feeTypeRepo;
    }

    // ========== Category CRUD ==========

    @Transactional
    public FeeCategoryEntity createCategory(String name) {
        if (categoryRepo.findByName(name).isPresent()) {
            throw new IllegalStateException("Category already exists: " + name);
        }
        FeeCategoryEntity entity = new FeeCategoryEntity();
        entity.setName(name);
        entity.setIsActive(true);
        return categoryRepo.save(entity);
    }

    @Transactional
    public FeeCategoryEntity updateCategory(Long id, String name) {
        FeeCategoryEntity entity = categoryRepo.findById(id)
                .orElseThrow(() -> new IllegalStateException("Category not found: id=" + id));
        
        if (!entity.getName().equals(name) && categoryRepo.findByName(name).isPresent()) {
            throw new IllegalStateException("Category name already exists: " + name);
        }
        
        entity.setName(name);
        return categoryRepo.save(entity);
    }

    @Transactional(readOnly = true)
    public List<FeeCategoryEntity> listCategories() {
        return categoryRepo.findAll();
    }

    @Transactional(readOnly = true)
    public FeeCategoryEntity getCategory(Long id) {
        return categoryRepo.findById(id)
                .orElseThrow(() -> new IllegalStateException("Category not found: id=" + id));
    }

    @Transactional
    public void disableCategory(Long id, Long replacementCategoryId) {
        if (replacementCategoryId == null) {
            throw new IllegalStateException("replacementCategoryId is required when disabling a category");
        }
        
        FeeCategoryEntity category = categoryRepo.findById(id)
                .orElseThrow(() -> new IllegalStateException("Category not found: id=" + id));
        
        FeeCategoryEntity replacement = categoryRepo.findById(replacementCategoryId)
                .orElseThrow(() -> new IllegalStateException("Replacement category not found: id=" + replacementCategoryId));
        
        if (!replacement.getIsActive()) {
            throw new IllegalStateException("Replacement category must be active");
        }
        
        if (id.equals(replacementCategoryId)) {
            throw new IllegalStateException("Cannot replace category with itself");
        }

        // Move all sub-FeeTypes to the new category BEFORE disabling
        List<FeeTypeEntity> feeTypes = feeTypeRepo.findByCategoryId(id);
        for (FeeTypeEntity feeType : feeTypes) {
            feeType.setCategoryId(replacementCategoryId);
            feeTypeRepo.save(feeType);
        }

        category.setIsActive(false);
        categoryRepo.save(category);
    }

    // ========== FeeType CRUD ==========

    @Transactional
    public FeeTypeEntity createFeeType(String name, Long categoryId, String defaultUnit, String allowedUnits, String fixedSpecOptions) {
        if (feeTypeRepo.findByName(name).isPresent()) {
            throw new IllegalStateException("FeeType already exists: " + name);
        }
        
        FeeCategoryEntity category = categoryRepo.findById(categoryId)
                .orElseThrow(() -> new IllegalStateException("Category not found: id=" + categoryId));
        
        if (!category.getIsActive()) {
            throw new IllegalStateException("Category is not active");
        }

        FeeTypeEntity entity = new FeeTypeEntity();
        entity.setName(name);
        entity.setCategoryId(categoryId);
        entity.setIsActive(true);
        entity.setDefaultUnit(defaultUnit);
        entity.setAllowedUnits(allowedUnits);
        entity.setFixedSpecOptions(fixedSpecOptions);
        return feeTypeRepo.save(entity);
    }

    @Transactional
    public FeeTypeEntity updateFeeType(Long id, String name, Long categoryId, String defaultUnit, String allowedUnits, String fixedSpecOptions) {
        FeeTypeEntity entity = feeTypeRepo.findById(id)
                .orElseThrow(() -> new IllegalStateException("FeeType not found: id=" + id));
        
        if (!entity.getName().equals(name) && feeTypeRepo.findByName(name).isPresent()) {
            throw new IllegalStateException("FeeType name already exists: " + name);
        }
        
        if (categoryId != null) {
            FeeCategoryEntity category = categoryRepo.findById(categoryId)
                    .orElseThrow(() -> new IllegalStateException("Category not found: id=" + categoryId));
            if (!category.getIsActive()) {
                throw new IllegalStateException("Category is not active");
            }
            entity.setCategoryId(categoryId);
        }
        
        entity.setName(name);
        entity.setDefaultUnit(defaultUnit);
        entity.setAllowedUnits(allowedUnits);
        entity.setFixedSpecOptions(fixedSpecOptions);
        return feeTypeRepo.save(entity);
    }

    @Transactional(readOnly = true)
    public List<FeeTypeEntity> listFeeTypes() {
        return feeTypeRepo.findAll();
    }

    @Transactional(readOnly = true)
    public List<FeeTypeEntity> listActiveFeeTypes() {
        return feeTypeRepo.findByIsActiveTrue();
    }

    @Transactional(readOnly = true)
    public FeeTypeEntity getFeeType(Long id) {
        return feeTypeRepo.findById(id)
                .orElseThrow(() -> new IllegalStateException("FeeType not found: id=" + id));
    }

    @Transactional
    public void disableFeeType(Long id, Long replacementFeeTypeId) {
        if (replacementFeeTypeId == null) {
            throw new IllegalStateException("replacementFeeTypeId is required when disabling a feeType");
        }
        
        FeeTypeEntity feeType = feeTypeRepo.findById(id)
                .orElseThrow(() -> new IllegalStateException("FeeType not found: id=" + id));
        
        FeeTypeEntity replacement = feeTypeRepo.findById(replacementFeeTypeId)
                .orElseThrow(() -> new IllegalStateException("Replacement feeType not found: id=" + replacementFeeTypeId));
        
        if (!replacement.getIsActive()) {
            throw new IllegalStateException("Replacement feeType must be active");
        }
        
        if (id.equals(replacementFeeTypeId)) {
            throw new IllegalStateException("Cannot replace feeType with itself");
        }

        feeType.setIsActive(false);
        feeType.setReplacementFeeTypeId(replacementFeeTypeId);
        feeTypeRepo.save(feeType);
    }
}

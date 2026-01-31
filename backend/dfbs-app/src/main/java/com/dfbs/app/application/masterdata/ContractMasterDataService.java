package com.dfbs.app.application.masterdata;

import com.dfbs.app.modules.masterdata.*;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.LocalDateTime;

@Service
public class ContractMasterDataService {

    private final ContractRepo contractRepo;

    public ContractMasterDataService(ContractRepo contractRepo) {
        this.contractRepo = contractRepo;
    }

    private static void requireNotBlank(String s, String field) {
        if (s == null || s.isBlank()) throw new IllegalArgumentException(field + " is required");
    }

    @Transactional
    public ContractEntity save(String contractNo, Long customerId, LocalDate startDate, LocalDate endDate,
                               String attachment, String createdBy) {
        requireNotBlank(contractNo, "contractNo");
        if (contractRepo.existsByContractNo(contractNo.trim())) {
            throw new IllegalArgumentException("contractNo already exists: " + contractNo);
        }
        if (attachment == null || attachment.isBlank()) {
            throw new IllegalArgumentException("attachment is required");
        }
        ContractEntity e = new ContractEntity();
        e.setContractNo(contractNo.trim());
        e.setCustomerId(customerId);
        e.setStartDate(startDate);
        e.setEndDate(endDate);
        e.setAttachment(attachment.trim());
        e.setStatus(MasterDataStatus.ENABLE);
        LocalDateTime now = LocalDateTime.now();
        e.setCreatedAt(now);
        e.setUpdatedAt(now);
        e.setCreatedBy(createdBy);
        e.setUpdatedBy(createdBy);
        return contractRepo.save(e);
    }

    @Transactional
    public ContractEntity update(Long id, String contractNo, Long customerId, LocalDate startDate, LocalDate endDate,
                                 String attachment, String updatedBy) {
        ContractEntity e = contractRepo.findById(id).orElseThrow(() -> new IllegalArgumentException("Contract not found: id=" + id));
        if (e.getStatus() == MasterDataStatus.DISABLE) {
            throw new IllegalStateException("Cannot update disabled contract");
        }
        if (contractNo != null && !contractNo.isBlank()) {
            if (!contractNo.trim().equals(e.getContractNo()) && contractRepo.existsByContractNo(contractNo.trim())) {
                throw new IllegalArgumentException("contractNo already exists: " + contractNo);
            }
            e.setContractNo(contractNo.trim());
        }
        if (customerId != null) e.setCustomerId(customerId);
        if (startDate != null) e.setStartDate(startDate);
        if (endDate != null) e.setEndDate(endDate);
        if (attachment != null) {
            if (attachment.isBlank()) throw new IllegalArgumentException("attachment cannot be empty");
            e.setAttachment(attachment.trim());
        }
        e.setUpdatedAt(LocalDateTime.now());
        e.setUpdatedBy(updatedBy);
        return contractRepo.save(e);
    }

    @Transactional(readOnly = true)
    public Page<ContractEntity> page(String keyword, MasterDataStatus status, Pageable pageable) {
        Specification<ContractEntity> spec = (root, query, cb) -> {
            var p = cb.conjunction();
            if (keyword != null && !keyword.isBlank()) {
                String k = "%" + keyword.toLowerCase() + "%";
                p = cb.and(p, cb.or(
                    cb.like(cb.lower(root.get("contractNo")), k)
                ));
            }
            if (status != null) p = cb.and(p, cb.equal(root.get("status"), status));
            return p;
        };
        return contractRepo.findAll(spec, pageable);
    }

    @Transactional
    public ContractEntity disable(Long id, String updatedBy) {
        ContractEntity e = contractRepo.findById(id).orElseThrow(() -> new IllegalArgumentException("Contract not found: id=" + id));
        e.setStatus(MasterDataStatus.DISABLE);
        e.setUpdatedAt(LocalDateTime.now());
        e.setUpdatedBy(updatedBy);
        return contractRepo.save(e);
    }

    @Transactional(readOnly = true)
    public java.util.Optional<ContractEntity> findById(Long id) {
        return contractRepo.findById(id);
    }
}

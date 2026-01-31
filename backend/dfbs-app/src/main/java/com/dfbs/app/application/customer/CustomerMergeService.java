package com.dfbs.app.application.customer;

import com.dfbs.app.application.customer.dto.CustomerMergeResponse;
import com.dfbs.app.modules.customer.CustomerAliasEntity;
import com.dfbs.app.modules.customer.CustomerAliasRepo;
import com.dfbs.app.modules.customer.CustomerEntity;
import com.dfbs.app.modules.customer.CustomerMergeLogEntity;
import com.dfbs.app.modules.customer.CustomerMergeLogRepo;
import com.dfbs.app.modules.customer.CustomerRepo;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.OffsetDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

@Service
public class CustomerMergeService {

    private final CustomerRepo customerRepo;
    private final CustomerAliasRepo aliasRepo;
    private final CustomerMergeLogRepo mergeLogRepo;
    private final ObjectMapper objectMapper = new ObjectMapper();

    public CustomerMergeService(CustomerRepo customerRepo, CustomerAliasRepo aliasRepo, CustomerMergeLogRepo mergeLogRepo) {
        this.customerRepo = customerRepo;
        this.aliasRepo = aliasRepo;
        this.mergeLogRepo = mergeLogRepo;
    }

    @Transactional
    public CustomerMergeResponse merge(Long targetId, Long sourceId, Map<String, Object> fieldOverrides,
                                       String mergeReason, String operatorId) {
        CustomerEntity target = customerRepo.findById(targetId)
                .orElseThrow(() -> new IllegalStateException("target customer not found: " + targetId));
        CustomerEntity source = customerRepo.findById(sourceId)
                .orElseThrow(() -> new IllegalStateException("source customer not found: " + sourceId));

        if (!"ACTIVE".equals(target.getStatus()) || target.getDeletedAt() != null) {
            throw new IllegalStateException("target customer is not ACTIVE");
        }
        if (!"ACTIVE".equals(source.getStatus()) || source.getDeletedAt() != null) {
            throw new IllegalStateException("source customer is not ACTIVE");
        }
        if (target.getId().equals(source.getId())) {
            throw new IllegalStateException("target and source must be different");
        }

        String sourceSnapshot = serializeCustomerState(source);
        String targetSnapshot = serializeCustomerState(target);

        CustomerMergeLogEntity log = new CustomerMergeLogEntity();
        log.setCreatedAt(OffsetDateTime.now());
        log.setCreatedBy(operatorId);
        log.setSourceCustomerId(source.getId());
        log.setTargetCustomerId(target.getId());
        log.setSourceSnapshot(sourceSnapshot);
        log.setTargetSnapshot(targetSnapshot);
        log.setMergeReason(mergeReason);
        log = mergeLogRepo.save(log);

        // Add source.name as alias to target (if not duplicate)
        if (source.getName() != null && !source.getName().isBlank()) {
            if (!aliasRepo.existsByAliasName(source.getName().trim())) {
                CustomerAliasEntity alias = CustomerAliasEntity.of(target, source.getName().trim());
                target.getAliases().add(alias);
            }
        }
        // Re-parent source aliases to target
        for (CustomerAliasEntity a : new ArrayList<>(source.getAliases())) {
            if (aliasRepo.existsByAliasName(a.getAliasName())) continue;
            source.getAliases().remove(a);
            a.setCustomer(target);
            target.getAliases().add(a);
        }

        // Apply field overrides to target
        if (fieldOverrides != null) {
            applyOverrides(target, fieldOverrides);
        }

        // Deactivate source: status=MERGED, mergedToId=target.id, free name
        String timestamp = OffsetDateTime.now().format(DateTimeFormatter.ofPattern("yyyyMMddHHmmss"));
        source.setStatus("MERGED");
        source.setMergedToId(target.getId());
        source.setName(source.getName() + "_MERGED_" + timestamp);
        source.setUpdatedAt(OffsetDateTime.now());

        customerRepo.save(target);
        customerRepo.save(source);

        return new CustomerMergeResponse(log.getId(), customerRepo.findById(targetId).orElseThrow());
    }

    @Transactional
    public void undo(Long logId) {
        CustomerMergeLogEntity log = mergeLogRepo.findById(logId)
                .orElseThrow(() -> new IllegalStateException("merge log not found: " + logId));

        CustomerEntity target = customerRepo.findById(log.getTargetCustomerId())
                .orElseThrow(() -> new IllegalStateException("target customer not found"));
        CustomerEntity source = customerRepo.findById(log.getSourceCustomerId())
                .orElseThrow(() -> new IllegalStateException("source customer not found"));

        CustomerState sourceState = deserializeCustomerState(log.getSourceSnapshot());
        CustomerState targetState = deserializeCustomerState(log.getTargetSnapshot());

        // Name conflict: would restoring source.name conflict with another ACTIVE customer?
        if (sourceState.name != null && !sourceState.name.isBlank()) {
            if (customerRepo.existsByNameAndStatusAndDeletedAtIsNullAndIdNot(sourceState.name.trim(), "ACTIVE", source.getId())) {
                throw new IllegalStateException("Name occupied: cannot undo, an ACTIVE customer already has name \"" + sourceState.name + "\"");
            }
        }

        // Restore source
        source.setName(sourceState.name);
        source.setStatus("ACTIVE");
        source.setMergedToId(null);
        source.setUpdatedAt(OffsetDateTime.now());
        source.getAliases().clear();
        for (String an : sourceState.aliasNames) {
            source.getAliases().add(CustomerAliasEntity.of(source, an));
        }
        customerRepo.save(source);

        // Restore target
        target.setName(targetState.name);
        target.setUpdatedAt(OffsetDateTime.now());
        target.getAliases().clear();
        for (String an : targetState.aliasNames) {
            target.getAliases().add(CustomerAliasEntity.of(target, an));
        }
        customerRepo.save(target);

        mergeLogRepo.delete(log);
    }

    private String serializeCustomerState(CustomerEntity c) {
        List<String> aliasNames = c.getAliases().stream()
                .map(CustomerAliasEntity::getAliasName)
                .toList();
        CustomerState state = new CustomerState(c.getName(), c.getCustomerCode(), c.getStatus(), c.getMergedToId(), aliasNames);
        try {
            return objectMapper.writeValueAsString(state);
        } catch (JsonProcessingException e) {
            throw new IllegalStateException("Failed to serialize customer state", e);
        }
    }

    private CustomerState deserializeCustomerState(String json) {
        if (json == null || json.isBlank()) return new CustomerState(null, null, "ACTIVE", null, List.of());
        try {
            return objectMapper.readValue(json, CustomerState.class);
        } catch (JsonProcessingException e) {
            throw new IllegalStateException("Failed to deserialize customer state", e);
        }
    }

    private void applyOverrides(CustomerEntity target, Map<String, Object> overrides) {
        if (overrides.containsKey("name")) {
            Object v = overrides.get("name");
            if (v != null) target.setName(v.toString().trim());
        }
        target.setUpdatedAt(OffsetDateTime.now());
    }

    private static class CustomerState {
        public String name;
        public String customerCode;
        public String status;
        public Long mergedToId;
        public List<String> aliasNames;

        @SuppressWarnings("unused")
        public CustomerState() {}

        public CustomerState(String name, String customerCode, String status, Long mergedToId, List<String> aliasNames) {
            this.name = name;
            this.customerCode = customerCode;
            this.status = status;
            this.mergedToId = mergedToId;
            this.aliasNames = aliasNames != null ? aliasNames : new ArrayList<>();
        }
    }
}

package com.dfbs.app.application.platformconfig;

import com.dfbs.app.application.platformconfig.dto.*;
import com.dfbs.app.modules.platformconfig.CodeValidatorType;
import com.dfbs.app.modules.platformconfig.PlatformConfigEntity;
import com.dfbs.app.modules.platformconfig.PlatformConfigRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
public class PlatformConfigService {

    private final PlatformConfigRepository repository;

    public PlatformConfigService(PlatformConfigRepository repository) {
        this.repository = repository;
    }

    @Transactional(readOnly = true)
    public List<PlatformOptionDto> getOptions() {
        return repository.findAllByIsActiveTrueOrderByPlatformCode().stream()
                .map(e -> new PlatformOptionDto(e.getPlatformName(), e.getPlatformCode()))
                .toList();
    }

    @Transactional(readOnly = true)
    public PlatformRulesDto getRulesByCode(String code) {
        PlatformConfigEntity e = repository.findByPlatformCode(code)
                .orElseThrow(() -> new IllegalArgumentException("平台不存在: " + code));
        return new PlatformRulesDto(
                e.getPlatformCode(),
                e.getPlatformName(),
                Boolean.TRUE.equals(e.getRuleUniqueEmail()),
                Boolean.TRUE.equals(e.getRuleUniquePhone()),
                Boolean.TRUE.equals(e.getRuleUniqueOrgName()),
                e.getCodeValidatorType() != null ? e.getCodeValidatorType() : CodeValidatorType.NONE
        );
    }

    @Transactional(readOnly = true)
    public List<PlatformConfigResponse> list() {
        return repository.findAll().stream()
                .map(PlatformConfigResponse::fromEntity)
                .toList();
    }

    @Transactional(readOnly = true)
    public PlatformConfigResponse get(Long id) {
        PlatformConfigEntity e = repository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("平台配置不存在: " + id));
        return PlatformConfigResponse.fromEntity(e);
    }

    @Transactional
    public PlatformConfigResponse create(PlatformConfigRequest request) {
        if (repository.findByPlatformCode(request.platformCode()).isPresent()) {
            throw new IllegalArgumentException("平台代码已存在: " + request.platformCode());
        }
        PlatformConfigEntity e = new PlatformConfigEntity();
        applyRequest(e, request);
        e = repository.save(e);
        return PlatformConfigResponse.fromEntity(e);
    }

    @Transactional
    public PlatformConfigResponse update(Long id, PlatformConfigRequest request) {
        PlatformConfigEntity e = repository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("平台配置不存在: " + id));
        applyRequest(e, request);
        e = repository.save(e);
        return PlatformConfigResponse.fromEntity(e);
    }

    @Transactional
    public PlatformConfigResponse toggleActive(Long id) {
        PlatformConfigEntity e = repository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("平台配置不存在: " + id));
        e.setIsActive(Boolean.FALSE.equals(e.getIsActive()));
        e = repository.save(e);
        return PlatformConfigResponse.fromEntity(e);
    }

    private void applyRequest(PlatformConfigEntity e, PlatformConfigRequest r) {
        e.setPlatformName(r.platformName());
        e.setPlatformCode(r.platformCode());
        e.setIsActive(r.isActive() != null ? r.isActive() : true);
        e.setRuleUniqueEmail(r.ruleUniqueEmail() != null ? r.ruleUniqueEmail() : false);
        e.setRuleUniquePhone(r.ruleUniquePhone() != null ? r.ruleUniquePhone() : false);
        e.setRuleUniqueOrgName(r.ruleUniqueOrgName() != null ? r.ruleUniqueOrgName() : false);
        e.setCodeValidatorType(r.codeValidatorType() != null ? r.codeValidatorType() : CodeValidatorType.NONE);
    }
}

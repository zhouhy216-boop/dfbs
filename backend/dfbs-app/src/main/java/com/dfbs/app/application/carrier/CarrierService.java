package com.dfbs.app.application.carrier;

import com.dfbs.app.modules.carrier.CarrierEntity;
import com.dfbs.app.modules.carrier.CarrierRepo;
import com.dfbs.app.modules.carrier.CarrierRuleEntity;
import com.dfbs.app.modules.carrier.CarrierRuleRepo;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
public class CarrierService {

    private final CarrierRepo carrierRepo;
    private final CarrierRuleRepo ruleRepo;

    public CarrierService(CarrierRepo carrierRepo, CarrierRuleRepo ruleRepo) {
        this.carrierRepo = carrierRepo;
        this.ruleRepo = ruleRepo;
    }

    @Transactional(readOnly = true)
    public List<CarrierEntity> list(boolean activeOnly) {
        return activeOnly ? carrierRepo.findByIsActiveTrue() : carrierRepo.findAll();
    }

    @Transactional(readOnly = true)
    public CarrierEntity getById(Long id) {
        return carrierRepo.findById(id)
                .orElseThrow(() -> new IllegalStateException("Carrier not found: id=" + id));
    }

    @Transactional
    public CarrierEntity create(String name, Boolean isActive) {
        if (name == null || name.isBlank()) throw new IllegalStateException("Carrier name cannot be blank");
        CarrierEntity e = new CarrierEntity();
        e.setName(name.trim());
        e.setIsActive(isActive != null ? isActive : true);
        return carrierRepo.save(e);
    }

    @Transactional
    public CarrierEntity update(Long id, String name, Boolean isActive) {
        CarrierEntity e = getById(id);
        if (name != null && !name.isBlank()) e.setName(name.trim());
        if (isActive != null) e.setIsActive(isActive);
        return carrierRepo.save(e);
    }

    @Transactional
    public void delete(Long id) {
        if (!carrierRepo.existsById(id)) throw new IllegalStateException("Carrier not found: id=" + id);
        carrierRepo.deleteById(id);
    }

    /**
     * Recommend carrier by address: first rule whose match_keyword is contained in address (rules sorted by priority DESC).
     */
    @Transactional(readOnly = true)
    public CarrierEntity recommendCarrier(String address) {
        if (address == null || address.isBlank()) return null;
        String normalized = address.trim();
        List<CarrierRuleEntity> rules = ruleRepo.findAllWithCarrierOrderByPriorityDesc();
        for (CarrierRuleEntity rule : rules) {
            if (rule.getMatchKeyword() != null && normalized.contains(rule.getMatchKeyword().trim())) {
                return rule.getCarrier();
            }
        }
        return null;
    }
}

package com.dfbs.app.application.settings;

import com.dfbs.app.modules.settings.BusinessLineEntity;
import com.dfbs.app.modules.settings.BusinessLineRepo;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
public class BusinessLineService {

    private final BusinessLineRepo repo;

    public BusinessLineService(BusinessLineRepo repo) {
        this.repo = repo;
    }

    @Transactional
    public BusinessLineEntity create(String name, String leaderIds) {
        if (repo.findByName(name).isPresent()) {
            throw new IllegalStateException("Business Line already exists: " + name);
        }
        BusinessLineEntity entity = new BusinessLineEntity();
        entity.setName(name);
        entity.setLeaderIds(leaderIds);
        entity.setIsActive(true);
        return repo.save(entity);
    }

    @Transactional
    public BusinessLineEntity update(Long id, String name, String leaderIds, Boolean isActive) {
        BusinessLineEntity entity = repo.findById(id)
                .orElseThrow(() -> new IllegalStateException("Business Line not found: id=" + id));
        
        if (name != null && !name.equals(entity.getName())) {
            if (repo.findByName(name).isPresent()) {
                throw new IllegalStateException("Business Line name already exists: " + name);
            }
            entity.setName(name);
        }
        
        if (leaderIds != null) {
            entity.setLeaderIds(leaderIds);
        }
        
        if (isActive != null) {
            entity.setIsActive(isActive);
        }
        
        return repo.save(entity);
    }

    @Transactional(readOnly = true)
    public List<BusinessLineEntity> list() {
        return repo.findAll();
    }

    @Transactional(readOnly = true)
    public List<BusinessLineEntity> listActive() {
        return repo.findByIsActiveTrue();
    }

    @Transactional(readOnly = true)
    public BusinessLineEntity get(Long id) {
        return repo.findById(id)
                .orElseThrow(() -> new IllegalStateException("Business Line not found: id=" + id));
    }
}

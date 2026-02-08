package com.dfbs.app.application.orgstructure;

import com.dfbs.app.modules.orgstructure.OrgChangeLogEntity;
import com.dfbs.app.modules.orgstructure.OrgChangeLogRepo;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;

@Service
public class OrgChangeLogService {

    private final OrgChangeLogRepo repo;

    public OrgChangeLogService(OrgChangeLogRepo repo) {
        this.repo = repo;
    }

    @Transactional
    public OrgChangeLogEntity log(String objectType, Long objectId, String action,
                                  Long operatorId, String operatorName, String summaryText, String diffJson) {
        OrgChangeLogEntity e = new OrgChangeLogEntity();
        e.setObjectType(objectType);
        e.setObjectId(objectId);
        e.setAction(action);
        e.setOperatorId(operatorId);
        e.setOperatorName(operatorName);
        e.setSummaryText(summaryText);
        e.setDiffJson(diffJson);
        return repo.save(e);
    }

    public Page<OrgChangeLogEntity> list(String objectType, Long operatorId, Instant from, Instant to, Pageable pageable) {
        return repo.findWithFilters(objectType, operatorId, from, to, pageable);
    }
}

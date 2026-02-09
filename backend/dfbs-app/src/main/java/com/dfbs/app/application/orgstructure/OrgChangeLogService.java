package com.dfbs.app.application.orgstructure;

import com.dfbs.app.modules.orgstructure.OrgChangeLogEntity;
import com.dfbs.app.modules.orgstructure.OrgChangeLogRepo;
import jakarta.persistence.criteria.Predicate;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.util.ArrayList;
import java.util.List;

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

    /**
     * List change logs with optional filters. Date predicates are added only when
     * from/to are non-null to avoid Postgres "could not determine data type of parameter"
     * when binding untyped nulls in comparisons.
     */
    public Page<OrgChangeLogEntity> list(String objectType, Long objectId, Long operatorId, Instant from, Instant to, Pageable pageable) {
        Specification<OrgChangeLogEntity> spec = (root, query, cb) -> {
            List<Predicate> predicates = new ArrayList<>();
            if (objectType != null && !objectType.isBlank()) {
                predicates.add(cb.equal(root.get("objectType"), objectType));
            }
            if (objectId != null) {
                predicates.add(cb.equal(root.get("objectId"), objectId));
            }
            if (operatorId != null) {
                predicates.add(cb.equal(root.get("operatorId"), operatorId));
            }
            if (from != null) {
                predicates.add(cb.greaterThanOrEqualTo(root.get("timestamp"), from));
            }
            if (to != null) {
                predicates.add(cb.lessThanOrEqualTo(root.get("timestamp"), to));
            }
            query.orderBy(cb.desc(root.get("timestamp")));
            return cb.and(predicates.toArray(new Predicate[0]));
        };
        return repo.findAll(spec, pageable);
    }
}

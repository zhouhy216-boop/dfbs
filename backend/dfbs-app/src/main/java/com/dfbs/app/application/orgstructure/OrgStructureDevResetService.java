package com.dfbs.app.application.orgstructure;

import com.dfbs.app.modules.orgstructure.OrgChangeLogRepo;
import com.dfbs.app.modules.orgstructure.OrgLevelEntity;
import com.dfbs.app.modules.orgstructure.OrgLevelPositionTemplateRepo;
import com.dfbs.app.modules.orgstructure.OrgNodeRepo;
import com.dfbs.app.modules.orgstructure.OrgPersonRepo;
import com.dfbs.app.modules.orgstructure.OrgPositionBindingRepo;
import com.dfbs.app.modules.orgstructure.OrgPositionEnabledRepo;
import com.dfbs.app.modules.orgstructure.PersonAffiliationRepo;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Dev-only: full wipe (nodes, people, affiliations, logs, levels).
 * Tooling reset: tree + logs + levels only (blocked if any people/affiliations; no config flag).
 * reset-all: clear org-structure domain test data in FK order, restore default levels + root node.
 */
@Service
public class OrgStructureDevResetService {

    private static final String COMPANY_LEVEL_DISPLAY_NAME = "公司";

    private final OrgChangeLogRepo changeLogRepo;
    private final PersonAffiliationRepo affiliationRepo;
    private final OrgPersonRepo personRepo;
    private final OrgNodeRepo nodeRepo;
    private final OrgLevelService levelService;
    private final OrgPositionBindingRepo bindingRepo;
    private final OrgPositionEnabledRepo enabledRepo;
    private final OrgLevelPositionTemplateRepo levelPositionTemplateRepo;
    private final OrgNodeService nodeService;

    public OrgStructureDevResetService(OrgChangeLogRepo changeLogRepo,
                                       PersonAffiliationRepo affiliationRepo,
                                       OrgPersonRepo personRepo,
                                       OrgNodeRepo nodeRepo,
                                       OrgLevelService levelService,
                                       OrgPositionBindingRepo bindingRepo,
                                       OrgPositionEnabledRepo enabledRepo,
                                       OrgLevelPositionTemplateRepo levelPositionTemplateRepo,
                                       OrgNodeService nodeService) {
        this.changeLogRepo = changeLogRepo;
        this.affiliationRepo = affiliationRepo;
        this.personRepo = personRepo;
        this.nodeRepo = nodeRepo;
        this.levelService = levelService;
        this.bindingRepo = bindingRepo;
        this.enabledRepo = enabledRepo;
        this.levelPositionTemplateRepo = levelPositionTemplateRepo;
        this.nodeService = nodeService;
    }

    /** For GET reset-availability: allowed, reason, personCount, affiliationCount, nodeCount. */
    public Map<String, Object> getResetToolingAvailability() {
        Map<String, Object> m = new HashMap<>();
        long personCount = personRepo.count();
        long affiliationCount = affiliationRepo.count();
        long nodeCount = nodeRepo.count();
        m.put("personCount", personCount);
        m.put("affiliationCount", affiliationCount);
        m.put("nodeCount", nodeCount);
        if (personCount > 0 || affiliationCount > 0) {
            m.put("allowed", false);
            m.put("reason", "存在人员/归属，禁止清空。人员数: " + personCount + "，归属数: " + affiliationCount);
            return m;
        }
        m.put("allowed", true);
        m.put("reason", null);
        return m;
    }

    /** Safe reset (tree + logs + levels only). Blocked if any people/affiliations. */
    @Transactional
    public List<OrgLevelEntity> resetTooling() {
        long personCount = personRepo.count();
        long affiliationCount = affiliationRepo.count();
        if (personCount > 0 || affiliationCount > 0) {
            throw new org.springframework.web.server.ResponseStatusException(
                    org.springframework.http.HttpStatus.BAD_REQUEST,
                    "存在人员/归属，禁止清空。人员数: " + personCount + "，归属数: " + affiliationCount);
        }
        nodeRepo.deleteAll();
        changeLogRepo.deleteAll();
        return levelService.resetLevelsToDefault();
    }

    /** Clear nodes, people, affiliations, change logs; restore default levels (公司 + 本部/部/课/系/班). */
    @Transactional
    public List<OrgLevelEntity> resetDev() {
        affiliationRepo.deleteAll();
        personRepo.deleteAll();
        nodeRepo.deleteAll();
        changeLogRepo.deleteAll();
        return levelService.resetLevelsToDefault();
    }

    /**
     * Reset all org-structure domain test data (local testing). Clears in safe FK order,
     * restores default levels and a root "公司" node. Does NOT touch accounts/permissions/templates.
     * Requires body.confirmText == "RESET".
     */
    @Transactional
    public Map<String, Object> resetAll() {
        long bindingCount = bindingRepo.count();
        long enabledCount = enabledRepo.count();
        long affiliationCount = affiliationRepo.count();
        long personCount = personRepo.count();
        long nodeCount = nodeRepo.count();
        long changeLogCount = changeLogRepo.count();
        long levelCount = levelService.listOrdered().size();

        bindingRepo.deleteAll();
        enabledRepo.deleteAll();
        affiliationRepo.deleteAll();
        personRepo.deleteAll();
        nodeRepo.deleteAll();
        changeLogRepo.deleteAll();
        levelPositionTemplateRepo.deleteAllInBatch();
        List<OrgLevelEntity> levels = levelService.resetLevelsToDefault();

        OrgLevelEntity companyLevel = levels.stream()
                .filter(l -> COMPANY_LEVEL_DISPLAY_NAME.equals(l.getDisplayName()))
                .findFirst()
                .orElseThrow();
        nodeService.create(companyLevel.getId(), null, "公司", null, true);

        Map<String, Object> summary = new HashMap<>();
        summary.put("positionBindingCleared", bindingCount);
        summary.put("positionEnabledCleared", enabledCount);
        summary.put("affiliationCleared", affiliationCount);
        summary.put("personCleared", personCount);
        summary.put("nodeCleared", nodeCount);
        summary.put("changeLogCleared", changeLogCount);
        summary.put("levelCleared", levelCount);
        summary.put("levelsRestored", levels.size());
        summary.put("rootNodeCreated", true);
        return summary;
    }
}

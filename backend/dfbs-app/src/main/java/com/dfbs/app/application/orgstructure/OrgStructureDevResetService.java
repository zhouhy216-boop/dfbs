package com.dfbs.app.application.orgstructure;

import com.dfbs.app.modules.orgstructure.OrgChangeLogRepo;
import com.dfbs.app.modules.orgstructure.OrgLevelEntity;
import com.dfbs.app.modules.orgstructure.OrgNodeRepo;
import com.dfbs.app.modules.orgstructure.OrgPersonRepo;
import com.dfbs.app.modules.orgstructure.PersonAffiliationRepo;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Dev-only: full wipe (nodes, people, affiliations, logs, levels).
 * Tooling reset: tree + logs + levels only (blocked if any people/affiliations; no config flag).
 */
@Service
public class OrgStructureDevResetService {

    private final OrgChangeLogRepo changeLogRepo;
    private final PersonAffiliationRepo affiliationRepo;
    private final OrgPersonRepo personRepo;
    private final OrgNodeRepo nodeRepo;
    private final OrgLevelService levelService;

    public OrgStructureDevResetService(OrgChangeLogRepo changeLogRepo,
                                       PersonAffiliationRepo affiliationRepo,
                                       OrgPersonRepo personRepo,
                                       OrgNodeRepo nodeRepo,
                                       OrgLevelService levelService) {
        this.changeLogRepo = changeLogRepo;
        this.affiliationRepo = affiliationRepo;
        this.personRepo = personRepo;
        this.nodeRepo = nodeRepo;
        this.levelService = levelService;
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
}

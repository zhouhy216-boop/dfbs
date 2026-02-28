package com.dfbs.app.application.perm;

import com.dfbs.app.interfaces.perm.PermAccountOverrideDto;
import com.dfbs.app.modules.orgstructure.JobLevelRepo;
import com.dfbs.app.modules.orgstructure.OrgNodeEntity;
import com.dfbs.app.modules.orgstructure.OrgPersonEntity;
import com.dfbs.app.modules.orgstructure.OrgPersonRepo;
import com.dfbs.app.modules.orgstructure.PersonAffiliationRepo;
import com.dfbs.app.modules.orgstructure.PersonAffiliationEntity;
import com.dfbs.app.modules.orgstructure.OrgNodeRepo;
import com.dfbs.app.modules.perm.PermRoleRepo;
import com.dfbs.app.modules.perm.PermUserRoleTemplateEntity;
import com.dfbs.app.modules.perm.PermUserRoleTemplateRepo;
import com.dfbs.app.modules.user.UserEntity;
import com.dfbs.app.modules.user.UserRepo;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * Admin-only account list: users with position, department, role template.
 * Uses app_user, org_person, person_affiliation, org_node, job_level, perm_user_role_template, perm_role.
 */
@Service
public class AccountListService {

    private static final int DEFAULT_LIMIT = 50;
    private static final int MAX_LIMIT = 200;

    private final UserRepo userRepo;
    private final OrgPersonRepo orgPersonRepo;
    private final JobLevelRepo jobLevelRepo;
    private final PersonAffiliationRepo affiliationRepo;
    private final OrgNodeRepo nodeRepo;
    private final PermUserRoleTemplateRepo userRoleTemplateRepo;
    private final PermRoleRepo roleRepo;

    public AccountListService(UserRepo userRepo,
                              OrgPersonRepo orgPersonRepo,
                              JobLevelRepo jobLevelRepo,
                              PersonAffiliationRepo affiliationRepo,
                              OrgNodeRepo nodeRepo,
                              PermUserRoleTemplateRepo userRoleTemplateRepo,
                              PermRoleRepo roleRepo) {
        this.userRepo = userRepo;
        this.orgPersonRepo = orgPersonRepo;
        this.jobLevelRepo = jobLevelRepo;
        this.affiliationRepo = affiliationRepo;
        this.nodeRepo = nodeRepo;
        this.userRoleTemplateRepo = userRoleTemplateRepo;
        this.roleRepo = roleRepo;
    }

    public List<PermAccountOverrideDto.AccountListItemResponse> getAccountList(String query, int limit) {
        int capped = Math.min(MAX_LIMIT, Math.max(1, limit));
        Pageable pageable = PageRequest.of(0, capped);

        List<UserEntity> users;
        if (query == null || query.trim().isEmpty()) {
            users = userRepo.findAllByOrderByUsernameAsc(pageable).getContent();
        } else {
            String q = query.trim();
            users = userRepo.findByUsernameContainingIgnoreCaseOrNicknameContainingIgnoreCaseOrderByUsername(q, q, pageable).getContent();
        }

        if (users.isEmpty()) {
            return List.of();
        }

        List<Long> userIds = users.stream().map(UserEntity::getId).toList();
        Set<Long> orgPersonIds = users.stream().map(UserEntity::getOrgPersonId).filter(Objects::nonNull).collect(Collectors.toSet());

        Map<Long, String> positionByPersonId = new HashMap<>();
        Map<Long, String> departmentByPersonId = new HashMap<>();
        if (!orgPersonIds.isEmpty()) {
            List<OrgPersonEntity> persons = orgPersonRepo.findAllById(orgPersonIds);
            Set<Long> jobLevelIds = persons.stream().map(OrgPersonEntity::getJobLevelId).filter(Objects::nonNull).collect(Collectors.toSet());
            Map<Long, String> jobLevelNames = new HashMap<>();
            if (!jobLevelIds.isEmpty()) {
                jobLevelRepo.findAllById(jobLevelIds).forEach(j -> jobLevelNames.put(j.getId(), j.getDisplayName()));
            }
            for (OrgPersonEntity p : persons) {
                if (p.getJobLevelId() != null) {
                    positionByPersonId.put(p.getId(), jobLevelNames.getOrDefault(p.getJobLevelId(), ""));
                }
            }
            for (Long personId : orgPersonIds) {
                affiliationRepo.findByPersonIdAndIsPrimaryTrue(personId)
                        .map(PersonAffiliationEntity::getOrgNodeId)
                        .flatMap(nodeRepo::findById)
                        .map(OrgNodeEntity::getName)
                        .ifPresent(name -> departmentByPersonId.put(personId, name));
            }
        }

        Map<Long, Long> roleIdByUserId = new HashMap<>();
        Map<Long, String> roleLabelByRoleId = new HashMap<>();
        List<PermUserRoleTemplateEntity> templates = userRoleTemplateRepo.findAllByUserIdIn(userIds);
        for (PermUserRoleTemplateEntity t : templates) {
            if (t.getRoleId() != null) {
                roleIdByUserId.put(t.getUserId(), t.getRoleId());
            }
        }
        Set<Long> roleIds = roleIdByUserId.values().stream().filter(Objects::nonNull).collect(Collectors.toSet());
        if (!roleIds.isEmpty()) {
            roleRepo.findAllById(roleIds).forEach(r -> roleLabelByRoleId.put(r.getId(), r.getLabel()));
        }

        List<PermAccountOverrideDto.AccountListItemResponse> result = new ArrayList<>();
        for (UserEntity u : users) {
            Long orgPersonId = u.getOrgPersonId();
            String position = orgPersonId != null ? positionByPersonId.getOrDefault(orgPersonId, "") : "";
            String department = orgPersonId != null ? departmentByPersonId.getOrDefault(orgPersonId, "") : "";
            Long roleTemplateId = roleIdByUserId.get(u.getId());
            String roleTemplateLabel = roleTemplateId != null ? roleLabelByRoleId.getOrDefault(roleTemplateId, "") : null;

            result.add(new PermAccountOverrideDto.AccountListItemResponse(
                    u.getId(),
                    u.getUsername(),
                    u.getNickname(),
                    u.getEnabled() != null ? u.getEnabled() : true,
                    orgPersonId,
                    position != null ? position : "",
                    department != null ? department : "",
                    roleTemplateId,
                    roleTemplateLabel
            ));
        }
        return result;
    }
}

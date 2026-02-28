package com.dfbs.app.application.account;

import com.dfbs.app.modules.user.UserRepo;
import org.springframework.stereotype.Service;

/**
 * Reusable validation for Person↔Account 1:1 binding. Used by admin account-creation (e.g. Step-02.c).
 */
@Service
public class AccountBindingValidationService {

    private final UserRepo userRepo;

    public AccountBindingValidationService(UserRepo userRepo) {
        this.userRepo = userRepo;
    }

    /**
     * Ensures the given org person is not already bound to an account.
     * @param orgPersonId org_person.id (must not be null)
     * @throws PersonAlreadyBoundException if an account already has this org_person_id
     */
    public void requirePersonNotYetBound(Long orgPersonId) {
        if (orgPersonId == null) {
            return;
        }
        userRepo.findByOrgPersonId(orgPersonId).ifPresent(existing -> {
            throw new PersonAlreadyBoundException("该人员已绑定账号，无法重复创建");
        });
    }
}

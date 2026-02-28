package com.dfbs.app.application.account;

import com.dfbs.app.application.perm.PermAccountOverrideService;
import com.dfbs.app.modules.orgstructure.OrgPersonEntity;
import com.dfbs.app.modules.orgstructure.OrgPersonRepo;
import com.dfbs.app.modules.user.UserEntity;
import com.dfbs.app.modules.user.UserRepo;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

/**
 * Admin-only account operations: create by binding person, enable/disable, reset password.
 */
@Service
public class AdminAccountService {

    private static final String DEFAULT_AUTHORITIES = "[\"ROLE_ADMIN\"]";

    private final UserRepo userRepo;
    private final AccountBindingValidationService bindingValidation;
    private final OrgPersonRepo orgPersonRepo;
    private final PasswordEncoder passwordEncoder;
    private final DefaultPasswordService defaultPasswordService;
    private final PermAccountOverrideService accountOverrideService;

    public AdminAccountService(UserRepo userRepo,
                              AccountBindingValidationService bindingValidation,
                              OrgPersonRepo orgPersonRepo,
                              PasswordEncoder passwordEncoder,
                              DefaultPasswordService defaultPasswordService,
                              PermAccountOverrideService accountOverrideService) {
        this.userRepo = userRepo;
        this.bindingValidation = bindingValidation;
        this.orgPersonRepo = orgPersonRepo;
        this.passwordEncoder = passwordEncoder;
        this.defaultPasswordService = defaultPasswordService;
        this.accountOverrideService = accountOverrideService;
    }

    /** Create account bound to org person; username globally unique; optional roleTemplateId applied via override. */
    @Transactional
    public AccountSummary createAccount(Long orgPersonId, String username, String nickname, Long roleTemplateId) {
        if (orgPersonId == null) {
            throw new IllegalArgumentException("orgPersonId required");
        }
        if (username == null || username.isBlank()) {
            throw new IllegalArgumentException("username required");
        }
        OrgPersonEntity person = orgPersonRepo.findById(orgPersonId)
                .orElseThrow(() -> new PersonNotFoundException("人员不存在: id=" + orgPersonId));
        if (!Boolean.TRUE.equals(person.getIsActive())) {
            throw new PersonNotActiveException("该人员已停用，无法创建账号");
        }
        bindingValidation.requirePersonNotYetBound(orgPersonId);
        if (userRepo.findByUsername(username.trim()).isPresent()) {
            throw new UsernameExistsException("用户名已存在: " + username);
        }
        String trimmedUsername = username.trim();
        UserEntity u = new UserEntity();
        u.setUsername(trimmedUsername);
        u.setNickname(nickname != null && !nickname.isBlank() ? nickname.trim() : null);
        u.setOrgPersonId(orgPersonId);
        u.setEnabled(true);
        u.setPasswordHash(passwordEncoder.encode(defaultPasswordService.getEffectiveDefaultPassword()));
        u.setAuthorities(DEFAULT_AUTHORITIES);
        u.setCanRequestPermission(false);
        u.setAllowNormalNotification(true);
        u.setCanManageStatements(false);
        try {
            u = userRepo.save(u);
        } catch (DataIntegrityViolationException e) {
            if (e.getMessage() != null && (e.getMessage().contains("uk_app_user_username") || e.getMessage().contains("username") || e.getMessage().contains("unique"))) {
                throw new UsernameExistsException("用户名已存在: " + trimmedUsername);
            }
            throw e;
        }
        if (roleTemplateId != null) {
            accountOverrideService.saveOverride(u.getId(), roleTemplateId, List.of(), List.of());
        }
        return new AccountSummary(u.getId(), u.getUsername(), u.getNickname(), u.getEnabled(), u.getOrgPersonId());
    }

    @Transactional
    public void setEnabled(Long userId, boolean enabled) {
        UserEntity u = userRepo.findById(userId).orElseThrow(() -> new UserNotFoundException("用户不存在: id=" + userId));
        u.setEnabled(enabled);
        userRepo.save(u);
    }

    @Transactional
    public void resetPassword(Long userId, String newPassword) {
        UserEntity u = userRepo.findById(userId).orElseThrow(() -> new UserNotFoundException("用户不存在: id=" + userId));
        String toEncode = (newPassword != null && !newPassword.isBlank()) ? newPassword : defaultPasswordService.getEffectiveDefaultPassword();
        u.setPasswordHash(passwordEncoder.encode(toEncode));
        userRepo.save(u);
    }

    public record AccountSummary(Long id, String username, String nickname, Boolean enabled, Long orgPersonId) {}

    public static final class UserNotFoundException extends RuntimeException {
        public UserNotFoundException(String message) { super(message); }
    }

    public static final class UsernameExistsException extends RuntimeException {
        public UsernameExistsException(String message) { super(message); }
    }

    public static final class PersonNotFoundException extends RuntimeException {
        public PersonNotFoundException(String message) { super(message); }
    }

    public static final class PersonNotActiveException extends RuntimeException {
        public PersonNotActiveException(String message) { super(message); }
    }
}

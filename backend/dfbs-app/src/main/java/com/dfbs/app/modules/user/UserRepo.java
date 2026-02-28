package com.dfbs.app.modules.user;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface UserRepo extends JpaRepository<UserEntity, Long> {

    Optional<UserEntity> findByUsername(String username);

    /** 1:1 binding: find account by org person id (for person-already-bound validation). */
    Optional<UserEntity> findByOrgPersonId(Long orgPersonId);

    /** Minimal search for PERM admin account selection: username or nickname contains (case-insensitive). */
    List<UserEntity> findTop20ByUsernameContainingIgnoreCaseOrNicknameContainingIgnoreCaseOrderByUsername(String username, String nickname);

    /** Paginated: first N accounts (internal staff). */
    Page<UserEntity> findAllByOrderByUsernameAsc(Pageable pageable);

    /** Paginated search: username or nickname contains (case-insensitive). */
    Page<UserEntity> findByUsernameContainingIgnoreCaseOrNicknameContainingIgnoreCaseOrderByUsername(String username, String nickname, Pageable pageable);
}

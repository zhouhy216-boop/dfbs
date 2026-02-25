package com.dfbs.app.modules.user;

import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface UserRepo extends JpaRepository<UserEntity, Long> {

    Optional<UserEntity> findByUsername(String username);

    /** Minimal search for PERM admin account selection: username or nickname contains (case-insensitive). */
    List<UserEntity> findTop20ByUsernameContainingIgnoreCaseOrNicknameContainingIgnoreCaseOrderByUsername(String username, String nickname);
}

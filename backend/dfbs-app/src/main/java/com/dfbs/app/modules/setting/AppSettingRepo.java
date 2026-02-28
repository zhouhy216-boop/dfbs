package com.dfbs.app.modules.setting;

import org.springframework.data.jpa.repository.JpaRepository;

public interface AppSettingRepo extends JpaRepository<AppSettingEntity, String> {
}

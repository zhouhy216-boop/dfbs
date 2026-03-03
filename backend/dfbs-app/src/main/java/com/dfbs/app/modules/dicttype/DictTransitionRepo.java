package com.dfbs.app.modules.dicttype;

import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface DictTransitionRepo extends JpaRepository<DictTransitionEntity, Long> {

    List<DictTransitionEntity> findByTypeIdOrderById(Long typeId);

    List<DictTransitionEntity> findByTypeIdAndEnabledOrderById(Long typeId, Boolean enabled);

    Optional<DictTransitionEntity> findByTypeIdAndFromItemIdAndToItemId(Long typeId, Long fromItemId, Long toItemId);
}

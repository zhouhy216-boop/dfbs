package com.dfbs.app.modules.orgstructure;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;

public interface OrgLevelRepo extends JpaRepository<OrgLevelEntity, Long> {

    List<OrgLevelEntity> findAllByOrderByOrderIndexAsc();

    List<OrgLevelEntity> findByIsEnabledTrueOrderByOrderIndexAsc();

    long count();

    /** Shift orderIndex by +1 for all configurable levels (exclude 公司) with orderIndex >= k. Single bulk update to avoid duplicates. */
    @Modifying
    @Query("UPDATE OrgLevelEntity e SET e.orderIndex = e.orderIndex + 1 WHERE e.orderIndex >= :k AND e.displayName <> :company")
    int shiftOrderIndexFrom(@Param("k") int k, @Param("company") String company);

    /** Phase-1 reorder: add offset to all configurable levels so no duplicate order_index during phase-2. */
    @Modifying
    @Query("UPDATE OrgLevelEntity e SET e.orderIndex = e.orderIndex + :offset WHERE e.displayName <> :company")
    int addOrderIndexOffset(@Param("offset") int offset, @Param("company") String company);

    /** Phase-1 insert: add offset to configurable levels with orderIndex >= k. Phase-3: subtract 999 where orderIndex >= 1000. */
    @Modifying
    @Query("UPDATE OrgLevelEntity e SET e.orderIndex = e.orderIndex + :offset WHERE e.orderIndex >= :k AND e.displayName <> :company")
    int addOrderIndexOffsetWhereGte(@Param("k") int k, @Param("offset") int offset, @Param("company") String company);
}

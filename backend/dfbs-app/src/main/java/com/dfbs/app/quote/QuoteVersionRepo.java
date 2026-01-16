package com.dfbs.app.quote;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.Optional;
import java.util.UUID;

public interface QuoteVersionRepo extends JpaRepository<QuoteVersionEntity, UUID> {

    /**
     * 同一个 quoteNo 的激活请求强制“排队”（串行执行）。
     * 这样并发激活时不会互相打架，接口表现更稳定。
     */
    @Query(value = "select pg_advisory_xact_lock(hashtext(:quoteNo))", nativeQuery = true)
    void lockQuoteNo(@Param("quoteNo") String quoteNo);

    /**
     * 先把该 quoteNo 下所有 active 的版本全部关掉（幂等：没有也不报错）。
     */
    @Modifying(clearAutomatically = true, flushAutomatically = true)
    @Query(value = "update quote_version set is_active = false where quote_no = :quoteNo and is_active = true", nativeQuery = true)
    int deactivateAllActiveByQuoteNo(@Param("quoteNo") String quoteNo);
    // ✅ 新增：按 (quoteNo, versionNo) 找到“已经存在的版本”
    Optional<QuoteVersionEntity> findByQuoteNoAndVersionNo(String quoteNo, Integer versionNo);

    // ✅ 新增：把某个已存在版本设为 active，并写 active_at
    @Modifying(clearAutomatically = true, flushAutomatically = true)
    @Query(value = "update quote_version set is_active = true, active_at = now() where id = :id", nativeQuery = true)
    int activateById(@Param("id") UUID id);
	@org.springframework.data.jpa.repository.Query(
		value = "select count(*) from quote_version where quote_no = :quoteNo and is_active = true",
		nativeQuery = true
	)
	long countActiveByQuoteNo(@org.springframework.data.repository.query.Param("quoteNo") String quoteNo);
	@org.springframework.data.jpa.repository.Modifying
	@org.springframework.data.jpa.repository.Query(
		value = """
			insert into quote_version
			(id, quote_no, version_no, is_active, active_at, created_at)
			values (:id, :quoteNo, :versionNo, :isActive, :activeAt, :createdAt)
			""",
		nativeQuery = true
	)
	void insertForTest(
		@org.springframework.data.repository.query.Param("id") java.util.UUID id,
		@org.springframework.data.repository.query.Param("quoteNo") String quoteNo,
		@org.springframework.data.repository.query.Param("versionNo") int versionNo,
		@org.springframework.data.repository.query.Param("isActive") boolean isActive,
		@org.springframework.data.repository.query.Param("activeAt") java.time.OffsetDateTime activeAt,
		@org.springframework.data.repository.query.Param("createdAt") java.time.OffsetDateTime createdAt
	);
}

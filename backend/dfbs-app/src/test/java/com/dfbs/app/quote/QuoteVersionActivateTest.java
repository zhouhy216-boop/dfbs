package com.dfbs.app.quote;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.transaction.annotation.Transactional;

import java.time.OffsetDateTime;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest
@Transactional
class QuoteVersionActivateTest {

    @Autowired
    private QuoteVersionService service;

    @Autowired
    private QuoteVersionRepo repo;

    @Test
    void activate_should_keep_only_one_active_per_quoteNo() {

        // 1) 生成一个“只属于本次测试”的 quoteNo
        String quoteNo = "TEST-" + UUID.randomUUID();

        // 2) 手工插入两个版本（都先是 inactive）
        repo.insertForTest(
            UUID.randomUUID(),
            quoteNo,
            1,
            false,
            OffsetDateTime.now(),
            OffsetDateTime.now()
        );

        repo.insertForTest(
            UUID.randomUUID(),
            quoteNo,
            2,
            false,
            OffsetDateTime.now(),
            OffsetDateTime.now()
        );

        // 3) 反复激活
        service.activate(quoteNo, 1);
        service.activate(quoteNo, 2);
        service.activate(quoteNo, 1);

        // 4) 验证：数据库中永远只有 1 条 active
        long activeCount = repo.countActiveByQuoteNo(quoteNo);

        assertThat(activeCount).isEqualTo(1);
    }
}

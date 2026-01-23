package com.dfbs.app.modules.quote;

import com.dfbs.app.application.quote.QuoteNumberService;
import com.dfbs.app.modules.quote.enums.QuoteSourceType;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.Import;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;

import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest
@Transactional
@Import(QuoteNumberingTestConfig.class)
class QuoteNumberingTest {

    @Autowired
    private QuoteNumberService numberService;

    @Autowired
    private TestClock testClock;

    @Test
    void sameUserSameMonth_seqIncrements() {
        String u = "alice";
        String a = numberService.generate(QuoteSourceType.WORK_ORDER, u);
        String b = numberService.generate(QuoteSourceType.WORK_ORDER, u);
        String c = numberService.generate(QuoteSourceType.WORK_ORDER, u);

        assertThat(a).endsWith("001");
        assertThat(b).endsWith("002");
        assertThat(c).endsWith("003");
        assertThat(a).startsWith("WO" + u + "260115");
        assertThat(b).startsWith("WO" + u + "260115");
        assertThat(c).startsWith("WO" + u + "260115");
    }

    @Test
    void sameUserNextMonth_seqResetsTo1() {
        String u = "bob";
        String first = numberService.generate(QuoteSourceType.WORK_ORDER, u);
        String second = numberService.generate(QuoteSourceType.WORK_ORDER, u);
        assertThat(first).endsWith("001");
        assertThat(second).endsWith("002");

        testClock.setInstant(Instant.parse("2026-02-15T10:00:00Z"));
        String nextMonth = numberService.generate(QuoteSourceType.WORK_ORDER, u);
        assertThat(nextMonth).endsWith("001");
        assertThat(nextMonth).contains("260215");
    }

    @Test
    void differentUsers_seqIndependent() {
        String a1 = numberService.generate(QuoteSourceType.WORK_ORDER, "alice");
        String b1 = numberService.generate(QuoteSourceType.WORK_ORDER, "bob");
        String a2 = numberService.generate(QuoteSourceType.WORK_ORDER, "alice");

        assertThat(a1).endsWith("001");
        assertThat(b1).endsWith("001");
        assertThat(a2).endsWith("002");
    }

    @Test
    void sourceType_prefixCorrect() {
        assertThat(numberService.generate(QuoteSourceType.WORK_ORDER, "u"))
                .startsWith("WO");
        assertThat(numberService.generate(QuoteSourceType.PLATFORM_FEE, "u"))
                .startsWith("PF");
        assertThat(numberService.generate(QuoteSourceType.ENTRUST_SHIPMENT, "u"))
                .startsWith("ES");
        assertThat(numberService.generate(QuoteSourceType.MANUAL, "u"))
                .startsWith("GN");
    }
}

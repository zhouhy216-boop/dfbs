package com.dfbs.app.modules.quote;

import com.dfbs.app.application.quote.QuoteService;
import com.dfbs.app.modules.quote.enums.Currency;
import com.dfbs.app.modules.quote.enums.QuoteSourceType;
import com.dfbs.app.modules.quote.enums.QuoteStatus;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.transaction.annotation.Transactional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@SpringBootTest
@Transactional
class QuoteStateTest {

    @Autowired
    private QuoteService quoteService;

    @Test
    void draft_canUpdateCurrency() {
        var cmd = new QuoteService.CreateQuoteCommand();
        cmd.setSourceType(QuoteSourceType.MANUAL);
        cmd.setCustomerId(1L);
        var created = quoteService.createDraft(cmd, "test-user");
        assertThat(created.getStatus()).isEqualTo(QuoteStatus.DRAFT);
        assertThat(created.getCurrency()).isNull();

        var update = new QuoteService.UpdateQuoteCommand();
        update.setCurrency(Currency.USD);
        var updated = quoteService.updateHeader(created.getId(), update);
        assertThat(updated.getCurrency()).isEqualTo(Currency.USD);
    }

    @Test
    void confirmed_updateThrowsException() {
        var cmd = new QuoteService.CreateQuoteCommand();
        cmd.setSourceType(QuoteSourceType.MANUAL);
        cmd.setCustomerId(1L);
        var created = quoteService.createDraft(cmd, "test-user");
        quoteService.confirm(created.getId());

        var update = new QuoteService.UpdateQuoteCommand();
        update.setCurrency(Currency.CNY);
        assertThatThrownBy(() -> quoteService.updateHeader(created.getId(), update))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("Cannot update");
    }

    @Test
    void cancel_statusChangesNumberRemains() {
        var cmd = new QuoteService.CreateQuoteCommand();
        cmd.setSourceType(QuoteSourceType.MANUAL);
        cmd.setCustomerId(1L);
        var created = quoteService.createDraft(cmd, "test-user");
        String quoteNoBefore = created.getQuoteNo();

        var cancelled = quoteService.cancel(created.getId());

        assertThat(cancelled.getStatus()).isEqualTo(QuoteStatus.CANCELLED);
        assertThat(cancelled.getQuoteNo()).isEqualTo(quoteNoBefore);
    }
}

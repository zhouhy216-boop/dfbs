package com.dfbs.app.application.quote;

import com.dfbs.app.modules.quote.QuoteEntity;
import com.dfbs.app.modules.quote.QuoteRepo;
import com.dfbs.app.modules.quote.enums.Currency;
import com.dfbs.app.modules.quote.enums.QuoteSourceType;
import com.dfbs.app.modules.quote.enums.QuoteStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class QuoteService {

    private final QuoteRepo quoteRepo;
    private final QuoteNumberService numberService;

    public QuoteService(QuoteRepo quoteRepo, QuoteNumberService numberService) {
        this.quoteRepo = quoteRepo;
        this.numberService = numberService;
    }

    /**
     * Creates a new quote as DRAFT. Validates customer exists (optional mock – skipped for MVP).
     */
    @Transactional
    public QuoteEntity createDraft(CreateQuoteCommand cmd, String currentUser) {
        String quoteNo = numberService.generate(cmd.getSourceType(), currentUser);
        QuoteEntity entity = new QuoteEntity();
        entity.setQuoteNo(quoteNo);
        entity.setStatus(QuoteStatus.DRAFT);
        entity.setSourceType(cmd.getSourceType());
        entity.setSourceRefId(cmd.getSourceRefId());
        entity.setCustomerId(cmd.getCustomerId());
        quoteRepo.save(entity);
        return entity;
    }

    @Transactional
    public QuoteEntity updateHeader(Long id, UpdateQuoteCommand cmd) {
        QuoteEntity entity = quoteRepo.findById(id)
                .orElseThrow(() -> new IllegalStateException("quote not found: id=" + id));
        if (entity.getStatus() != QuoteStatus.DRAFT) {
            throw new IllegalStateException("Cannot update confirmed or cancelled quote");
        }
        if (cmd.getCurrency() != null) {
            entity.setCurrency(cmd.getCurrency());
        }
        if (cmd.getRecipient() != null) {
            entity.setRecipient(cmd.getRecipient());
        }
        if (cmd.getPhone() != null) {
            entity.setPhone(cmd.getPhone());
        }
        if (cmd.getAddress() != null) {
            entity.setAddress(cmd.getAddress());
        }
        return quoteRepo.save(entity);
    }

    @Transactional
    public QuoteEntity confirm(Long id) {
        QuoteEntity entity = quoteRepo.findById(id)
                .orElseThrow(() -> new IllegalStateException("quote not found: id=" + id));
        entity.setStatus(QuoteStatus.CONFIRMED);
        return quoteRepo.save(entity);
    }

    @Transactional
    public QuoteEntity cancel(Long id) {
        QuoteEntity entity = quoteRepo.findById(id)
                .orElseThrow(() -> new IllegalStateException("quote not found: id=" + id));
        entity.setStatus(QuoteStatus.CANCELLED);
        return quoteRepo.save(entity);
    }

    @Transactional(readOnly = true)
    public java.util.Optional<QuoteEntity> findById(Long id) {
        return quoteRepo.findById(id);
    }

    /** Command for creating a draft quote. */
    public static final class CreateQuoteCommand {
        private QuoteSourceType sourceType;
        private String sourceRefId;
        private Long customerId;

        public QuoteSourceType getSourceType() { return sourceType; }
        public void setSourceType(QuoteSourceType sourceType) { this.sourceType = sourceType; }
        public String getSourceRefId() { return sourceRefId; }
        public void setSourceRefId(String sourceRefId) { this.sourceRefId = sourceRefId; }
        public Long getCustomerId() { return customerId; }
        public void setCustomerId(Long customerId) { this.customerId = customerId; }
    }

    /** Command for updating quote header (DRAFT only). */
    public static final class UpdateQuoteCommand {
        private Currency currency;
        private String recipient;
        private String phone;
        private String address;

        public Currency getCurrency() { return currency; }
        public void setCurrency(Currency currency) { this.currency = currency; }
        public String getRecipient() { return recipient; }
        public void setRecipient(String recipient) { this.recipient = recipient; }
        public String getPhone() { return phone; }
        public void setPhone(String phone) { this.phone = phone; }
        public String getAddress() { return address; }
        public void setAddress(String address) { this.address = address; }
    }
}

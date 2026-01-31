package com.dfbs.app.application.correction;

import com.dfbs.app.application.quote.QuoteNumberService;
import com.dfbs.app.application.quote.QuoteService;
import com.dfbs.app.modules.quote.QuoteEntity;
import com.dfbs.app.modules.quote.QuoteItemEntity;
import com.dfbs.app.modules.quote.QuoteItemRepo;
import com.dfbs.app.modules.quote.QuoteRepo;
import com.dfbs.app.modules.quote.enums.QuotePaymentStatus;
import com.dfbs.app.modules.quote.enums.QuoteSourceType;
import com.dfbs.app.modules.quote.enums.QuoteStatus;
import com.dfbs.app.modules.quote.enums.QuoteVoidStatus;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.List;

@Component
public class QuoteCorrectionExecutor implements CorrectionExecutor {

    private final QuoteService quoteService;
    private final QuoteRepo quoteRepo;
    private final QuoteItemRepo quoteItemRepo;
    private final QuoteNumberService quoteNumberService;

    public QuoteCorrectionExecutor(QuoteService quoteService, QuoteRepo quoteRepo,
                                    QuoteItemRepo quoteItemRepo, QuoteNumberService quoteNumberService) {
        this.quoteService = quoteService;
        this.quoteRepo = quoteRepo;
        this.quoteItemRepo = quoteItemRepo;
        this.quoteNumberService = quoteNumberService;
    }

    @Override
    @Transactional
    public void voidOld(Long id) {
        quoteService.cancel(id);
    }

    @Override
    @Transactional
    public Long createNew(Long oldId, String changesJson, Long createdBy) {
        QuoteEntity old = quoteRepo.findById(oldId)
                .orElseThrow(() -> new IllegalStateException("Quote not found: id=" + oldId));
        String creatorName = createdBy != null ? "corr-" + createdBy : "correction";
        String newQuoteNo = quoteNumberService.generate(QuoteSourceType.MANUAL, creatorName);

        QuoteEntity neu = new QuoteEntity();
        neu.setQuoteNo(newQuoteNo);
        neu.setStatus(QuoteStatus.DRAFT);
        neu.setSourceType(old.getSourceType());
        neu.setSourceRefId(old.getSourceRefId());
        neu.setSourceId(old.getSourceId());
        neu.setMachineInfo(old.getMachineInfo());
        neu.setMachineId(old.getMachineId());
        neu.setAssigneeId(old.getAssigneeId());
        neu.setCustomerConfirmerId(old.getCustomerConfirmerId());
        neu.setCustomerId(old.getCustomerId());
        neu.setCustomerName(old.getCustomerName());
        neu.setOriginalCustomerName(old.getOriginalCustomerName());
        neu.setRecipient(old.getRecipient());
        neu.setPhone(old.getPhone());
        neu.setAddress(old.getAddress());
        neu.setCurrency(old.getCurrency());
        neu.setPaymentStatus(QuotePaymentStatus.UNPAID);
        neu.setVoidStatus(QuoteVoidStatus.NONE);
        neu.setCollectorId(old.getCollectorId());
        neu.setParentQuoteId(oldId);
        neu.setBusinessLineId(old.getBusinessLineId());
        neu.setPaidAmount(java.math.BigDecimal.ZERO);
        neu.setInvoicedAmount(old.getInvoicedAmount() != null ? old.getInvoicedAmount() : java.math.BigDecimal.ZERO);
        neu.setFirstSubmissionTime(null);
        neu.setCreatedBy(createdBy != null ? String.valueOf(createdBy) : null);
        neu = quoteRepo.save(neu);

        List<QuoteItemEntity> oldItems = quoteItemRepo.findByQuoteIdOrderByLineOrderAsc(oldId);
        for (QuoteItemEntity oi : oldItems) {
            QuoteItemEntity ni = new QuoteItemEntity();
            ni.setQuoteId(neu.getId());
            ni.setLineOrder(oi.getLineOrder());
            ni.setExpenseType(oi.getExpenseType());
            ni.setFeeTypeId(oi.getFeeTypeId());
            ni.setPartId(oi.getPartId());
            ni.setDescription(oi.getDescription());
            ni.setOriginalPartName(oi.getOriginalPartName());
            ni.setSpec(oi.getSpec());
            ni.setUnit(oi.getUnit());
            ni.setQuantity(oi.getQuantity());
            ni.setUnitPrice(oi.getUnitPrice());
            ni.setStandardPrice(oi.getStandardPrice());
            ni.setIsPriceDeviated(oi.getIsPriceDeviated());
            ni.setAmount(oi.getAmount());
            ni.setWarehouse(oi.getWarehouse());
            ni.setRemark(oi.getRemark());
            ni.setPriceSourceInfo(oi.getPriceSourceInfo());
            ni.setManualPriceReason(oi.getManualPriceReason());
            quoteItemRepo.save(ni);
        }
        return neu.getId();
    }
}

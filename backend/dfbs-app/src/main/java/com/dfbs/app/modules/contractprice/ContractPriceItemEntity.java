package com.dfbs.app.modules.contractprice;

import com.dfbs.app.modules.quote.enums.Currency;
import com.dfbs.app.modules.quote.enums.QuoteExpenseType;
import jakarta.persistence.*;

import java.math.BigDecimal;

@Entity
@Table(name = "contract_price_item")
public class ContractPriceItemEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "header_id", nullable = false)
    private ContractPriceHeaderEntity header;

    @Enumerated(EnumType.STRING)
    @Column(name = "item_type", nullable = false, length = 32)
    private QuoteExpenseType itemType;

    @Column(name = "unit_price", nullable = false, precision = 19, scale = 2)
    private BigDecimal unitPrice;

    @Enumerated(EnumType.STRING)
    @Column(name = "currency", nullable = false, length = 16)
    private Currency currency = Currency.CNY;

    public ContractPriceItemEntity() {}

    public Long getId() { return id; }
    public ContractPriceHeaderEntity getHeader() { return header; }
    public void setHeader(ContractPriceHeaderEntity header) { this.header = header; }
    public QuoteExpenseType getItemType() { return itemType; }
    public void setItemType(QuoteExpenseType itemType) { this.itemType = itemType; }
    public BigDecimal getUnitPrice() { return unitPrice; }
    public void setUnitPrice(BigDecimal unitPrice) { this.unitPrice = unitPrice; }
    public Currency getCurrency() { return currency; }
    public void setCurrency(Currency currency) { this.currency = currency; }
}

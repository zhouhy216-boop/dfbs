package com.dfbs.app.modules.contractprice;

import com.dfbs.app.modules.quote.enums.QuoteExpenseType;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface ContractPriceItemRepo extends JpaRepository<ContractPriceItemEntity, Long> {

    List<ContractPriceItemEntity> findByHeader_IdAndItemType(Long headerId, QuoteExpenseType itemType);
}

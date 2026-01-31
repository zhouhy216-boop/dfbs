package com.dfbs.app.modules.contractprice;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;

public interface ContractPriceHeaderRepo extends JpaRepository<ContractPriceHeaderEntity, Long>,
        JpaSpecificationExecutor<ContractPriceHeaderEntity> {

    List<ContractPriceHeaderEntity> findByCustomerIdAndStatusOrderByPriorityDesc(Long customerId, ContractStatus status);

    @Query("SELECT DISTINCT h FROM ContractPriceHeaderEntity h LEFT JOIN FETCH h.items WHERE h.customerId = :customerId AND h.status = :status ORDER BY h.priority DESC")
    List<ContractPriceHeaderEntity> findByCustomerIdAndStatusWithItems(@Param("customerId") Long customerId, @Param("status") ContractStatus status);
}

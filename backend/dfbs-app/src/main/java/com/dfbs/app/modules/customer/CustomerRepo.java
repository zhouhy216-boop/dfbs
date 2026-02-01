package com.dfbs.app.modules.customer;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;

import java.util.List;
import java.util.Optional;

public interface CustomerRepo extends JpaRepository<CustomerEntity, Long>, JpaSpecificationExecutor<CustomerEntity> {

    /**
     * 注意：customer_code 是业务主键（全局唯一，不复用），
     * 即使软删除（deleted_at 有值）也仍然占用该号码。
     */
    Optional<CustomerEntity> findByCustomerCode(String customerCode);

    /**
     * 用于业务读取“未删除”的记录
     */
    Optional<CustomerEntity> findByCustomerCodeAndDeletedAtIsNull(String customerCode);

    Optional<CustomerEntity> findByIdAndDeletedAtIsNull(Long id);

    /**
     * Unique name (active only): exists another ACTIVE, non-deleted customer with this name.
     */
    boolean existsByNameAndStatusAndDeletedAtIsNull(String name, String status);

    /**
     * Unique name (active only), excluding given id (for update).
     */
    boolean existsByNameAndStatusAndDeletedAtIsNullAndIdNot(String name, String status, Long id);

    /** Find customers by name (for import: resolve customerName to id). */
    List<CustomerEntity> findByNameAndDeletedAtIsNull(String name);
}

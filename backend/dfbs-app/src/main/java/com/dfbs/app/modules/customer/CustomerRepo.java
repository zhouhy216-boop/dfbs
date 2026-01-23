package com.dfbs.app.modules.customer;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;

import java.util.Optional;
import java.util.UUID;

public interface CustomerRepo extends JpaRepository<CustomerEntity, UUID>, JpaSpecificationExecutor<CustomerEntity> {

    /**
     * 注意：customer_code 是业务主键（全局唯一，不复用），
     * 即使软删除（deleted_at 有值）也仍然占用该号码。
     */
    Optional<CustomerEntity> findByCustomerCode(String customerCode);

    /**
     * 用于业务读取“未删除”的记录
     */
    Optional<CustomerEntity> findByCustomerCodeAndDeletedAtIsNull(String customerCode);

    Optional<CustomerEntity> findByIdAndDeletedAtIsNull(UUID id);
}

package com.dfbs.app.application.customer;

import com.dfbs.app.modules.customer.CustomerRepo;
import org.springframework.stereotype.Service;

/**
 * 主数据 Customer 的“唯一写入口”（占坑）。
 *
 * 当前阶段（3.22）规则：
 * - 只创建空骨架，不引入任何业务逻辑
 * - 未来所有对 Customer 主数据的写入（新增/修改/作废/软删除）只能收敛到这里
 * - 其他模块禁止直接调用 CustomerRepo.save/delete（由 ArchUnit 守门）
 */
@Service
public class CustomerMasterDataService {

    private final CustomerRepo customerRepo;

    public CustomerMasterDataService(CustomerRepo customerRepo) {
        this.customerRepo = customerRepo;
    }

    // 3.22：先不提供任何方法，避免引入写逻辑与新业务假设
}

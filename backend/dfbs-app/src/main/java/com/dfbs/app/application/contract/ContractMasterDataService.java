package com.dfbs.app.application.contract;

import com.dfbs.app.modules.contract.ContractRepo;
import org.springframework.stereotype.Service;

/**
 * 主数据 Contract 的“唯一写入口”（占坑）。
 *
 * 3.22：仅骨架占坑，不写业务逻辑。
 * 未来 contract 的写入只能从这里进入，避免 repo 写入散落导致返工。
 */
@Service
public class ContractMasterDataService {

    private final ContractRepo contractRepo;

    public ContractMasterDataService(ContractRepo contractRepo) {
        this.contractRepo = contractRepo;
    }

    // 3.22：空实现
}

package com.dfbs.app.application.iccid;

import com.dfbs.app.modules.iccid.IccidRepo;
import org.springframework.stereotype.Service;

/**
 * 主数据 ICCID 的“唯一写入口”（占坑）。
 *
 * 3.22：仅骨架占坑，不写业务逻辑。
 */
@Service
public class IccidMasterDataService {

    private final IccidRepo iccidRepo;

    public IccidMasterDataService(IccidRepo iccidRepo) {
        this.iccidRepo = iccidRepo;
    }

    // 3.22：空实现
}

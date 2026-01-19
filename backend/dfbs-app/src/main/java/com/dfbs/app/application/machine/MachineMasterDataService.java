package com.dfbs.app.application.machine;

import com.dfbs.app.modules.machine.MachineRepo;
import org.springframework.stereotype.Service;

/**
 * 主数据 Machine 的“唯一写入口”（占坑）。
 *
 * 3.22：仅骨架占坑，不写业务逻辑。
 */
@Service
public class MachineMasterDataService {

    private final MachineRepo machineRepo;

    public MachineMasterDataService(MachineRepo machineRepo) {
        this.machineRepo = machineRepo;
    }

    // 3.22：空实现
}

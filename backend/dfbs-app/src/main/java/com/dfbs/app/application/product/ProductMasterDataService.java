package com.dfbs.app.application.product;

import com.dfbs.app.modules.product.ProductRepo;
import org.springframework.stereotype.Service;

/**
 * 主数据 Product 的“唯一写入口”（占坑）。
 *
 * 3.22：仅骨架占坑，不写业务逻辑。
 */
@Service
public class ProductMasterDataService {

    private final ProductRepo productRepo;

    public ProductMasterDataService(ProductRepo productRepo) {
        this.productRepo = productRepo;
    }

    // 3.22：空实现
}

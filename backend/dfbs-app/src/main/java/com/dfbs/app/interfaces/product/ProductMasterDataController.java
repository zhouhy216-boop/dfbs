package com.dfbs.app.interfaces.product;

import com.dfbs.app.application.product.ProductMasterDataService;
import com.dfbs.app.modules.product.ProductEntity;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping("/api/masterdata/products")
public class ProductMasterDataController {

    private final ProductMasterDataService service;

    public ProductMasterDataController(ProductMasterDataService service) {
        this.service = service;
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public ProductEntity create(@RequestBody Map<String, String> body) {
        return service.create(
                body.get("productCode"),
                body.get("name")
        );
    }
}

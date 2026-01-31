package com.dfbs.app.interfaces.product;

import com.dfbs.app.application.product.ProductMasterDataService;
import com.dfbs.app.modules.product.ProductEntity;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@Tag(name = "MasterData (Product)", description = "Product CRUD and search")
@RestController
public class ProductMasterDataController {

    private final ProductMasterDataService service;

    public ProductMasterDataController(ProductMasterDataService service) {
        this.service = service;
    }

    @PostMapping("/api/masterdata/products")
    @ResponseStatus(HttpStatus.CREATED)
    public ProductEntity create(@RequestBody Map<String, String> body) {
        return service.create(
                body.get("productCode"),
                body.get("name")
        );
    }

    @Operation(summary = "Search products", description = "Paginated search by keyword")
    @GetMapping("/api/v1/products")
    public ResponseEntity<Page<ProductDto>> search(
            @RequestParam(required = false) String keyword,
            Pageable pageable
    ) {
        Page<ProductEntity> page = service.search(keyword, pageable);
        Page<ProductDto> dtoPage = page.map(ProductDto::from);
        return ResponseEntity.ok(dtoPage);
    }
}

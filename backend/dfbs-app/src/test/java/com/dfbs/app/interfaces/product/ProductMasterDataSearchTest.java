package com.dfbs.app.interfaces.product;

import com.dfbs.app.modules.product.ProductEntity;
import com.dfbs.app.modules.product.ProductRepo;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import java.time.OffsetDateTime;
import java.util.UUID;

import static org.hamcrest.Matchers.hasItem;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
@AutoConfigureMockMvc
class ProductMasterDataSearchTest {

    @Autowired
    private MockMvc mvc;

    @Autowired
    private ProductRepo repo;

    @Test
    void testSearchByName() throws Exception {
        String productCode = "PROD-SEARCH-NAME-" + System.currentTimeMillis();
        String uniqueName = "UniqueProductName" + System.currentTimeMillis();

        // Create product
        mvc.perform(post("/api/masterdata/products")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "productCode": "%s",
                                  "name": "%s"
                                }
                                """.formatted(productCode, uniqueName)))
                .andExpect(status().isCreated());

        // Search by full unique name - should return at least 1 (may include old data with same prefix)
        // Use hasItem to check if the list contains our expected item, regardless of position
        mvc.perform(get("/api/v1/products?keyword=" + uniqueName + "&page=0&size=10"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content.length()").value(org.hamcrest.Matchers.greaterThanOrEqualTo(1)))
                .andExpect(jsonPath("$.content[*].name", hasItem(uniqueName)));
    }

    @Test
    void testSearchByProductCode() throws Exception {
        String uniqueCode = "PROD-CODE-" + System.currentTimeMillis();
        String productName = "Product Name";

        // Create product
        mvc.perform(post("/api/masterdata/products")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "productCode": "%s",
                                  "name": "%s"
                                }
                                """.formatted(uniqueCode, productName)))
                .andExpect(status().isCreated());

        // Search by full unique productCode - should return at least 1 (may include old data with same prefix)
        // Use hasItem to check if the list contains our expected item, regardless of position
        mvc.perform(get("/api/v1/products?keyword=" + uniqueCode + "&page=0&size=10"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content.length()").value(org.hamcrest.Matchers.greaterThanOrEqualTo(1)))
                .andExpect(jsonPath("$.content[*].productCode", hasItem(uniqueCode)));
    }

    @Test
    void testSoftDeleteExcluded() throws Exception {
        String productCode = "PROD-SEARCH-DEL-" + System.currentTimeMillis();

        // Create product
        mvc.perform(post("/api/masterdata/products")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "productCode": "%s",
                                  "name": "Will Be Deleted"
                                }
                                """.formatted(productCode)))
                .andExpect(status().isCreated());

        ProductEntity entity = repo.findByProductCode(productCode)
                .orElseThrow();

        // Soft delete
        entity.setDeletedAt(OffsetDateTime.now());
        repo.save(entity);

        // List all - should not return deleted product
        String response = mvc.perform(get("/api/v1/products?page=0&size=10"))
                .andExpect(status().isOk())
                .andReturn()
                .getResponse()
                .getContentAsString();
        
        // Verify deleted product is not in the response
        org.assertj.core.api.Assertions.assertThat(response).doesNotContain(productCode);
    }
}

package com.dfbs.app.interfaces.product;

import com.dfbs.app.modules.product.ProductRepo;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
class ProductMasterDataCreateTest {

    @Autowired
    private MockMvc mvc;

    @Autowired
    private ProductRepo productRepo;

    @Test
    void can_create_product() throws Exception {
        String productCode = "PROD-" + System.currentTimeMillis();

        mvc.perform(post("/api/masterdata/products")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "productCode": "%s",
                                  "name": "Test Product"
                                }
                                """.formatted(productCode)))
                .andExpect(status().isCreated());
    }
}

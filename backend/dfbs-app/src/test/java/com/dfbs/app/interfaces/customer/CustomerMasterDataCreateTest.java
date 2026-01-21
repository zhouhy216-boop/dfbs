package com.dfbs.app.interfaces.customer;

import com.dfbs.app.modules.customer.CustomerRepo;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import java.util.UUID;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
@AutoConfigureMockMvc
class CustomerMasterDataCreateTest {

    @Autowired
    private MockMvc mvc;

    @Autowired
    private CustomerRepo customerRepo;

    @Test
    void create_customer_should_return_201() throws Exception {

        String customerNo = "CUST-" + System.currentTimeMillis();

        String body = """
                {
                  "customerNo": "%s",
                  "name": "ACME Co."
                }
                """.formatted(customerNo);

        mvc.perform(post("/api/masterdata/customers")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(body))
                .andExpect(status().isCreated())
                .andExpect(content().contentTypeCompatibleWith(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.customerNo").value(customerNo))
                .andExpect(jsonPath("$.name").value("ACME Co."))
                .andExpect(jsonPath("$.createdAt").exists());
    }

    @Test
    void cannot_recreate_customerNo_after_soft_delete_because_business_key_not_reusable() throws Exception {
        String customerNo = "CUST-DELETE-TEST";

        // 1) create
        String body1 = """
                {
                  "customerNo": "%s",
                  "name": "To Be Deleted"
                }
                """.formatted(customerNo);

        mvc.perform(post("/api/masterdata/customers")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(body1))
                .andExpect(status().isCreated());

        UUID id = customerRepo.findByCustomerCodeAndDeletedAtIsNull(customerNo)
                .orElseThrow()
                .getId();

        // 2) soft delete
        mvc.perform(delete("/api/masterdata/customers/{id}", id))
                .andExpect(status().isNoContent());

        // 3) recreate with same customerNo -> 必须失败（409）
        String body2 = """
                {
                  "customerNo": "%s",
                  "name": "Recreated"
                }
                """.formatted(customerNo);

        mvc.perform(post("/api/masterdata/customers")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(body2))
                .andExpect(status().isConflict());
    }
}

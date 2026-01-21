package com.dfbs.app.interfaces.customer;

import com.dfbs.app.modules.customer.CustomerRepo;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import java.util.UUID;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
@AutoConfigureMockMvc
class CustomerMasterDataCreateTest {

    @Autowired
    private MockMvc mvc;

    @Autowired
    private CustomerRepo repo;

    @Test
    void can_read_customer_by_id() throws Exception {
        String customerNo = "CUST-READ-" + System.currentTimeMillis();

        // create
        mvc.perform(post("/api/masterdata/customers")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "customerNo": "%s",
                                  "name": "Read Me"
                                }
                                """.formatted(customerNo)))
                .andExpect(status().isCreated());

        UUID id = repo.findByCustomerCodeAndDeletedAtIsNull(customerNo)
                .orElseThrow()
                .getId();

        // read
        mvc.perform(get("/api/masterdata/customers/{id}", id))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.name").value("Read Me"));
    }

    @Test
    void cannot_read_deleted_customer() throws Exception {
        String customerNo = "CUST-READ-DEL-" + System.currentTimeMillis();

        // create
        mvc.perform(post("/api/masterdata/customers")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "customerNo": "%s",
                                  "name": "Will Be Deleted"
                                }
                                """.formatted(customerNo)))
                .andExpect(status().isCreated());

        UUID id = repo.findByCustomerCodeAndDeletedAtIsNull(customerNo)
                .orElseThrow()
                .getId();

        // delete
        mvc.perform(delete("/api/masterdata/customers/{id}", id))
                .andExpect(status().isNoContent());

        // read -> 404
        mvc.perform(get("/api/masterdata/customers/{id}", id))
                .andExpect(status().isNotFound());
    }
}

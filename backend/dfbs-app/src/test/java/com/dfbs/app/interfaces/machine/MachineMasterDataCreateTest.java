package com.dfbs.app.interfaces.machine;

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
class MachineMasterDataCreateTest {

    @Autowired
    private MockMvc mvc;

    @Test
    void can_create_machine() throws Exception {
        String customerCode = "CUST-M-" + System.currentTimeMillis();
        String contractNo  = "CON-M-" + System.currentTimeMillis();
        String productCode = "PROD-M-" + System.currentTimeMillis();
        String machineSn   = "SN-" + System.currentTimeMillis();

        // create customer
        mvc.perform(post("/api/masterdata/customers")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "customerNo": "%s",
                                  "name": "For Machine"
                                }
                                """.formatted(customerCode)))
                .andExpect(status().isCreated());

        // create product
        mvc.perform(post("/api/masterdata/products")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "productCode": "%s",
                                  "name": "Machine Product"
                                }
                                """.formatted(productCode)))
                .andExpect(status().isCreated());

        // create contract
        mvc.perform(post("/api/masterdata/contracts")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "contractNo": "%s",
                                  "customerCode": "%s"
                                }
                                """.formatted(contractNo, customerCode)))
                .andExpect(status().isCreated());

        // create machine
        mvc.perform(post("/api/masterdata/machines")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "machineSn": "%s",
                                  "contractNo": "%s",
                                  "productCode": "%s"
                                }
                                """.formatted(machineSn, contractNo, productCode)))
                .andExpect(status().isCreated());
    }
}

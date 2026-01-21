package com.dfbs.app.interfaces.contract;

import com.dfbs.app.modules.contract.ContractRepo;
import com.dfbs.app.modules.customer.CustomerRepo;
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
class ContractMasterDataCreateTest {

    @Autowired
    private MockMvc mvc;

    @Autowired
    private CustomerRepo customerRepo;

    @Autowired
    private ContractRepo contractRepo;

    @Test
    void can_create_contract() throws Exception {
        String customerCode = "CUST-CON-" + System.currentTimeMillis();
        String contractNo = "CON-" + System.currentTimeMillis();

        // create customer first（依赖外键）
        mvc.perform(post("/api/masterdata/customers")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "customerNo": "%s",
                                  "name": "For Contract"
                                }
                                """.formatted(customerCode)))
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
    }
}

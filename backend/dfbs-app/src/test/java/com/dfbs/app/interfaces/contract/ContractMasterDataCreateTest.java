package com.dfbs.app.interfaces.contract;

import com.dfbs.app.modules.customer.CustomerEntity;
import com.dfbs.app.modules.customer.CustomerRepo;
import com.dfbs.app.modules.masterdata.ContractRepo;
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

        CustomerEntity customer = CustomerEntity.create(customerCode, "For Contract");
        customerRepo.save(customer);

        mvc.perform(post("/api/v1/masterdata/contracts")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "contractNo": "%s",
                                  "customerId": %d,
                                  "attachment": "{}",
                                  "createdBy": "test"
                                }
                                """.formatted(contractNo, customer.getId())))
                .andExpect(status().isCreated());
    }
}

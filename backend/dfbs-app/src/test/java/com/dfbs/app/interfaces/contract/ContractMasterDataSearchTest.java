package com.dfbs.app.interfaces.contract;

import com.dfbs.app.modules.customer.CustomerEntity;
import com.dfbs.app.modules.customer.CustomerRepo;
import com.dfbs.app.modules.masterdata.ContractEntity;
import com.dfbs.app.modules.masterdata.ContractRepo;
import com.dfbs.app.modules.masterdata.MasterDataStatus;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import java.util.UUID;

import static org.hamcrest.Matchers.hasItem;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
@AutoConfigureMockMvc
class ContractMasterDataSearchTest {

    @Autowired
    private MockMvc mvc;

    @Autowired
    private ContractRepo contractRepo;

    @Autowired
    private CustomerRepo customerRepo;

    @Test
    void testSearchByContractNo() throws Exception {
        // Create customer first
        String customerCode = "CUST-CONTRACT-" + System.currentTimeMillis();
        CustomerEntity customer = CustomerEntity.create(customerCode, "Test Customer " + UUID.randomUUID());
        customerRepo.save(customer);

        String contractNo = "CONTRACT-SEARCH-" + System.currentTimeMillis();

        // Create contract via new masterdata API (customerId, attachment required)
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

        // Search by full unique contractNo
        mvc.perform(get("/api/v1/masterdata/contracts?keyword=" + contractNo + "&page=0&size=10"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content.length()").value(org.hamcrest.Matchers.greaterThanOrEqualTo(1)))
                .andExpect(jsonPath("$.content[*].contractNo", hasItem(contractNo)));
    }

    @Test
    void testSearchNoMatch() throws Exception {
        // Create customer first
        String customerCode = "CUST-CONTRACT-NOMATCH-" + System.currentTimeMillis();
        CustomerEntity customer = CustomerEntity.create(customerCode, "Test Customer " + UUID.randomUUID());
        customerRepo.save(customer);

        String contractNo = "CONTRACT-NOMATCH-" + System.currentTimeMillis();

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

        mvc.perform(get("/api/v1/masterdata/contracts?keyword=NonExistentRandomString12345&page=0&size=10"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content.length()").value(0))
                .andExpect(jsonPath("$.totalElements").value(0));
    }

    @Test
    void testSoftDeleteExcluded() throws Exception {
        String customerCode = "CUST-CONTRACT-DEL-" + System.currentTimeMillis();
        CustomerEntity customer = CustomerEntity.create(customerCode, "Test Customer " + UUID.randomUUID());
        customerRepo.save(customer);

        String contractNo = "CONTRACT-DEL-" + System.currentTimeMillis();

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

        ContractEntity entity = contractRepo.findByContractNo(contractNo).orElseThrow();

        // Soft delete (status DISABLE)
        entity.setStatus(MasterDataStatus.DISABLE);
        contractRepo.save(entity);

        String response = mvc.perform(get("/api/v1/masterdata/contracts?page=0&size=10"))
                .andExpect(status().isOk())
                .andReturn()
                .getResponse()
                .getContentAsString();

        org.assertj.core.api.Assertions.assertThat(response).doesNotContain(contractNo);
    }
}

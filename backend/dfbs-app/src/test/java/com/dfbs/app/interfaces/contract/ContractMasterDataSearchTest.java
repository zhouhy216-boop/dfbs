package com.dfbs.app.interfaces.contract;

import com.dfbs.app.modules.contract.ContractEntity;
import com.dfbs.app.modules.contract.ContractRepo;
import com.dfbs.app.modules.customer.CustomerEntity;
import com.dfbs.app.modules.customer.CustomerRepo;
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
        CustomerEntity customer = CustomerEntity.create(customerCode, "Test Customer");
        customerRepo.save(customer);

        String contractNo = "CONTRACT-SEARCH-" + System.currentTimeMillis();

        // Create contract
        mvc.perform(post("/api/masterdata/contracts")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "contractNo": "%s",
                                  "customerCode": "%s"
                                }
                                """.formatted(contractNo, customerCode)))
                .andExpect(status().isCreated());

        // Search by full unique contractNo - should return at least 1 (may include old data with same prefix)
        // Use hasItem to check if the list contains our expected item, regardless of position
        mvc.perform(get("/api/v1/contracts?keyword=" + contractNo + "&page=0&size=10"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content.length()").value(org.hamcrest.Matchers.greaterThanOrEqualTo(1)))
                .andExpect(jsonPath("$.content[*].contractNo", hasItem(contractNo)));
    }

    @Test
    void testSearchNoMatch() throws Exception {
        // Create customer first
        String customerCode = "CUST-CONTRACT-NOMATCH-" + System.currentTimeMillis();
        CustomerEntity customer = CustomerEntity.create(customerCode, "Test Customer");
        customerRepo.save(customer);

        String contractNo = "CONTRACT-NOMATCH-" + System.currentTimeMillis();

        // Create contract
        mvc.perform(post("/api/masterdata/contracts")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "contractNo": "%s",
                                  "customerCode": "%s"
                                }
                                """.formatted(contractNo, customerCode)))
                .andExpect(status().isCreated());

        // Search random string - should return 0
        mvc.perform(get("/api/v1/contracts?keyword=NonExistentRandomString12345&page=0&size=10"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content.length()").value(0))
                .andExpect(jsonPath("$.totalElements").value(0));
    }

    @Test
    void testSoftDeleteExcluded() throws Exception {
        // Create customer first
        String customerCode = "CUST-CONTRACT-DEL-" + System.currentTimeMillis();
        CustomerEntity customer = CustomerEntity.create(customerCode, "Test Customer");
        customerRepo.save(customer);

        String contractNo = "CONTRACT-DEL-" + System.currentTimeMillis();

        // Create contract
        mvc.perform(post("/api/masterdata/contracts")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "contractNo": "%s",
                                  "customerCode": "%s"
                                }
                                """.formatted(contractNo, customerCode)))
                .andExpect(status().isCreated());

        ContractEntity entity = contractRepo.findByContractNo(contractNo)
                .orElseThrow();

        // Soft delete
        entity.setDeletedAt(java.time.OffsetDateTime.now());
        contractRepo.save(entity);

        // List all - should not return deleted contract
        String response = mvc.perform(get("/api/v1/contracts?page=0&size=10"))
                .andExpect(status().isOk())
                .andReturn()
                .getResponse()
                .getContentAsString();
        
        // Verify deleted contract is not in the response
        org.assertj.core.api.Assertions.assertThat(response).doesNotContain(contractNo);
    }
}

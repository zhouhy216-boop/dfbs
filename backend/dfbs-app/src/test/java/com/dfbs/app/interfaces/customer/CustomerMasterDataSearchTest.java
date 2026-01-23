package com.dfbs.app.interfaces.customer;

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
class CustomerMasterDataSearchTest {

    @Autowired
    private MockMvc mvc;

    @Autowired
    private CustomerRepo repo;

    @Test
    void testListAll() throws Exception {
        String customerNo1 = "CUST-SEARCH-1-" + System.currentTimeMillis();
        String customerNo2 = "CUST-SEARCH-2-" + System.currentTimeMillis();

        // Create 2 customers
        mvc.perform(post("/api/masterdata/customers")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "customerNo": "%s",
                                  "name": "Customer One"
                                }
                                """.formatted(customerNo1)))
                .andExpect(status().isCreated());

        mvc.perform(post("/api/masterdata/customers")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "customerNo": "%s",
                                  "name": "Customer Two"
                                }
                                """.formatted(customerNo2)))
                .andExpect(status().isCreated());

        // List all - should return 2
        mvc.perform(get("/api/v1/customers?page=0&size=10"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content.length()").value(org.hamcrest.Matchers.greaterThanOrEqualTo(2)))
                .andExpect(jsonPath("$.totalElements").value(org.hamcrest.Matchers.greaterThanOrEqualTo(2)));
    }

    @Test
    void testSearchMatch() throws Exception {
        String customerNo = "CUST-SEARCH-MATCH-" + System.currentTimeMillis();
        String uniqueName = "UniqueSearchName" + System.currentTimeMillis();

        // Create customer
        mvc.perform(post("/api/masterdata/customers")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "customerNo": "%s",
                                  "name": "%s"
                                }
                                """.formatted(customerNo, uniqueName)))
                .andExpect(status().isCreated());

        // Search by full unique name - should return at least 1 (may include old data with same prefix)
        // Use hasItem to check if the list contains our expected item, regardless of position
        mvc.perform(get("/api/v1/customers?keyword=" + uniqueName + "&page=0&size=10"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content.length()").value(org.hamcrest.Matchers.greaterThanOrEqualTo(1)))
                .andExpect(jsonPath("$.content[*].name", hasItem(uniqueName)));
    }

    @Test
    void testSearchNoMatch() throws Exception {
        String customerNo = "CUST-SEARCH-NOMATCH-" + System.currentTimeMillis();

        // Create customer
        mvc.perform(post("/api/masterdata/customers")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "customerNo": "%s",
                                  "name": "Normal Customer"
                                }
                                """.formatted(customerNo)))
                .andExpect(status().isCreated());

        // Search random string - should return 0
        mvc.perform(get("/api/v1/customers?keyword=NonExistentRandomString12345&page=0&size=10"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content.length()").value(0))
                .andExpect(jsonPath("$.totalElements").value(0));
    }

    @Test
    void testDeletedExcluded() throws Exception {
        String customerNo = "CUST-SEARCH-DEL-" + System.currentTimeMillis();

        // Create customer
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

        // Soft delete
        mvc.perform(delete("/api/masterdata/customers/{id}", id))
                .andExpect(status().isNoContent());

        // List all - should not return deleted customer
        String response = mvc.perform(get("/api/v1/customers?page=0&size=10"))
                .andExpect(status().isOk())
                .andReturn()
                .getResponse()
                .getContentAsString();
        
        // Verify deleted customer is not in the response
        org.assertj.core.api.Assertions.assertThat(response).doesNotContain(customerNo);
    }
}

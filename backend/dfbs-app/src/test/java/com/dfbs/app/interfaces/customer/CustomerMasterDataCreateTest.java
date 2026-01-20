package com.dfbs.app.interfaces.customer;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
@AutoConfigureMockMvc
class CustomerMasterDataCreateTest {

    @Autowired
    private MockMvc mvc;

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
}

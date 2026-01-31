package com.dfbs.app.infra;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.ResultActions;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

/**
 * Integration test: OpenAPI docs and Swagger UI are exposed; error contract (machineCode) is documented.
 */
@SpringBootTest
@AutoConfigureMockMvc
class SwaggerTest {

    @Autowired
    private MockMvc mvc;

    @Test
    void apiDocs_returns200_andContainsOpenApiStructure_andErrorContract() throws Exception {
        ResultActions result = mvc.perform(get("/v3/api-docs"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.openapi").exists())
                .andExpect(jsonPath("$.paths").exists())
                .andExpect(jsonPath("$.components").exists());

        String body = result.andReturn().getResponse().getContentAsString();
        org.assertj.core.api.Assertions.assertThat(body)
                .contains("machineCode")
                .as("Error contract (machineCode) must be present in API docs");
    }
}

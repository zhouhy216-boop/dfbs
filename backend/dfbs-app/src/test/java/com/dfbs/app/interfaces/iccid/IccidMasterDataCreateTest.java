package com.dfbs.app.interfaces.iccid;

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
class IccidMasterDataCreateTest {

    @Autowired
    private MockMvc mvc;

    @Test
    void can_create_iccid_without_machine() throws Exception {
        String iccidNo = "ICCID-" + System.currentTimeMillis();

        mvc.perform(post("/api/masterdata/iccid")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "iccidNo": "%s"
                                }
                                """.formatted(iccidNo)))
                .andExpect(status().isCreated());
    }
}

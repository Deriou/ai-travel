package cn.edu.hebau.aitravel.util;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.web.servlet.MockMvc;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

/** 验证异常也按 Result 格式返回，且 HTTP 状态与 code 一致。 */
@SpringBootTest(properties = {
        "spring.datasource.url=jdbc:h2:mem:error-test;MODE=MySQL;DB_CLOSE_DELAY=-1",
        "spring.datasource.username=sa",
        "spring.datasource.password="
})
@AutoConfigureMockMvc
class GlobalExceptionHandlerTests {
    @Autowired
    private MockMvc mockMvc;

    @Test
    void unknownPathReturns404Result() throws Exception {
        mockMvc.perform(get("/not-exist"))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.code").value(404))
                .andExpect(jsonPath("$.data").doesNotExist());
    }

    @Test
    void wrongMethodReturns400Result() throws Exception {
        mockMvc.perform(post("/city/list"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.code").value(400))
                .andExpect(jsonPath("$.msg").value("请求参数格式不正确"));
    }
}

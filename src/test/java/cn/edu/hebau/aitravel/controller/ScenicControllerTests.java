package cn.edu.hebau.aitravel.controller;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.jdbc.Sql;
import org.springframework.test.web.servlet.MockMvc;

import static org.hamcrest.Matchers.hasSize;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

/** 使用 H2 中的 5 个景点（北京 3 个、杭州 2 个）验证筛选、分页和参数校验。 */
@SpringBootTest(properties = {
        "spring.datasource.url=jdbc:h2:mem:scenic-test;MODE=MySQL;DB_CLOSE_DELAY=-1",
        "spring.datasource.username=sa",
        "spring.datasource.password=",
        "jwt.secret=test-secret-key-for-unit-tests-only-32chars"
})
@AutoConfigureMockMvc
@Sql("/scenic-test.sql")
class ScenicControllerTests {
    @Autowired
    private MockMvc mockMvc;

    @Test
    void returnsAllScenicsPagedWhenCityIdIsMissing() throws Exception {
        mockMvc.perform(get("/scenic/list").param("pageNum", "2").param("pageSize", "2"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(200))
                .andExpect(jsonPath("$.data.total").value(5))
                .andExpect(jsonPath("$.data.pageNum").value(2))
                .andExpect(jsonPath("$.data.pageSize").value(2))
                .andExpect(jsonPath("$.data.list", hasSize(2)))
                .andExpect(jsonPath("$.data.list[0].id").value(3))
                .andExpect(jsonPath("$.data.list[0].cityId").value(1))
                .andExpect(jsonPath("$.data.list[0].scenicName").value("颐和园"))
                .andExpect(jsonPath("$.data.list[0].scenicDesc").value("北京景点3"));
    }

    @Test
    void filtersByCity() throws Exception {
        mockMvc.perform(get("/scenic/list").param("cityId", "2").param("pageNum", "1").param("pageSize", "5"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.total").value(2))
                .andExpect(jsonPath("$.data.list", hasSize(2)))
                .andExpect(jsonPath("$.data.list[0].scenicName").value("西湖"));
    }

    @Test
    void returnsEmptyListWhenNoMatchOrPageOutOfRange() throws Exception {
        mockMvc.perform(get("/scenic/list").param("cityId", "99").param("pageNum", "1").param("pageSize", "5"))
                .andExpect(jsonPath("$.data.total").value(0))
                .andExpect(jsonPath("$.data.list", hasSize(0)));

        mockMvc.perform(get("/scenic/list").param("pageNum", "10").param("pageSize", "5"))
                .andExpect(jsonPath("$.data.total").value(5))
                .andExpect(jsonPath("$.data.list", hasSize(0)));
    }

    @Test
    void rejectsInvalidPageParams() throws Exception {
        mockMvc.perform(get("/scenic/list").param("pageNum", "0").param("pageSize", "5"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.code").value(400))
                .andExpect(jsonPath("$.msg").value("页码不能小于1"));

        mockMvc.perform(get("/scenic/list").param("pageNum", "1").param("pageSize", "101"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.msg").value("每页条数应在1到100之间"));

        mockMvc.perform(get("/scenic/list").param("pageNum", "abc").param("pageSize", "5"))
                .andExpect(status().isBadRequest());

        mockMvc.perform(get("/scenic/list").param("pageSize", "5"))
                .andExpect(status().isBadRequest());
    }
}

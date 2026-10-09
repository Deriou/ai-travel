package cn.edu.hebau.aitravel.controller;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.test.context.jdbc.Sql;
import org.springframework.test.web.servlet.MockMvc;

import static org.hamcrest.Matchers.hasSize;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

/** 通过独立 H2 数据库验证完整查询链路，不改动本地 MySQL。 */
@SpringBootTest(properties = {
        "spring.datasource.url=jdbc:h2:mem:city-test;MODE=MySQL;DB_CLOSE_DELAY=-1",
        "spring.datasource.username=sa",
        "spring.datasource.password="
})
@AutoConfigureMockMvc
@Sql("/city-test.sql")
class CityControllerTests {
    @Autowired
    private MockMvc mockMvc;
    @Autowired
    private JdbcTemplate jdbcTemplate;

    @Test
    void returnsCitiesInIdOrderWithCamelCaseNames() throws Exception {
        jdbcTemplate.update("INSERT INTO city (id, city_name, description) VALUES (?, ?, ?)",
                2L, "杭州", "西湖");
        jdbcTemplate.update("INSERT INTO city (id, city_name, description) VALUES (?, ?, ?)",
                1L, "北京", "首都");

        mockMvc.perform(get("/city/list"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(200))
                .andExpect(jsonPath("$.msg").value("success"))
                .andExpect(jsonPath("$.data", hasSize(2)))
                .andExpect(jsonPath("$.data[0].id").value(1))
                .andExpect(jsonPath("$.data[0].cityName").value("北京"))
                .andExpect(jsonPath("$.data[0].description").value("首都"))
                .andExpect(jsonPath("$.data[1].cityName").value("杭州"));
    }

    @Test
    void returnsEmptyArrayWhenCityTableIsEmpty() throws Exception {
        mockMvc.perform(get("/city/list"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(200))
                .andExpect(jsonPath("$.msg").value("success"))
                .andExpect(jsonPath("$.data", hasSize(0)));
    }
}

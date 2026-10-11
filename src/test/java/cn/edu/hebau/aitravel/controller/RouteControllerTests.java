package cn.edu.hebau.aitravel.controller;

import cn.edu.hebau.aitravel.util.JwtUtil;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.test.context.jdbc.Sql;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.ResultActions;

import static org.hamcrest.Matchers.hasSize;
import static org.hamcrest.Matchers.matchesPattern;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

/** 验证路线的保存、查询、删除、收藏，以及只能操作本人路线。用户 1 和用户 2 分别持有自己的令牌。 */
@SpringBootTest(properties = {
        "spring.datasource.url=jdbc:h2:mem:route-test;MODE=MySQL;DB_CLOSE_DELAY=-1",
        "spring.datasource.username=sa",
        "spring.datasource.password=",
        "jwt.secret=test-secret-key-for-unit-tests-only-32chars"
})
@AutoConfigureMockMvc
@Sql("/route-test.sql")
class RouteControllerTests {
    private static final String ROUTE_JSON = """
            {"destination":"北京","days":3,"preference":"休闲、美食",
             "routeContent":"第1天：故宫。\\n第2天：颐和园。","tipsContent":"提前预约。"}
            """;

    @Autowired
    private MockMvc mockMvc;
    @Autowired
    private JwtUtil jwtUtil;
    @Autowired
    private JdbcTemplate jdbcTemplate;

    private String auth(long userId) {
        return "Bearer " + jwtUtil.createToken(userId);
    }

    private ResultActions save(long userId, String json) throws Exception {
        return mockMvc.perform(post("/route/save").header("Authorization", auth(userId))
                .contentType(MediaType.APPLICATION_JSON).content(json));
    }

    private long saveAndGetId(long userId) throws Exception {
        save(userId, ROUTE_JSON).andExpect(status().isOk());
        return jdbcTemplate.queryForObject("SELECT MAX(id) FROM travel_route", Long.class);
    }

    private ResultActions collect(long userId, long id, String json) throws Exception {
        return mockMvc.perform(put("/route/collect/" + id).header("Authorization", auth(userId))
                .contentType(MediaType.APPLICATION_JSON).content(json));
    }

    @Test
    void saveThenListOwnRoutes() throws Exception {
        save(1, ROUTE_JSON)
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.msg").value("保存成功"))
                .andExpect(jsonPath("$.data").isNumber());

        mockMvc.perform(get("/route/myList").header("Authorization", auth(1)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data", hasSize(1)))
                .andExpect(jsonPath("$.data[0].destination").value("北京"))
                .andExpect(jsonPath("$.data[0].days").value(3))
                .andExpect(jsonPath("$.data[0].routeContent").value("第1天：故宫。\n第2天：颐和园。"))
                .andExpect(jsonPath("$.data[0].tipsContent").value("提前预约。"))
                .andExpect(jsonPath("$.data[0].isCollect").value(0))
                .andExpect(jsonPath("$.data[0].createTime")
                        .value(matchesPattern("\\d{4}-\\d{2}-\\d{2} \\d{2}:\\d{2}:\\d{2}")))
                .andExpect(jsonPath("$.data[0].userId").doesNotExist());

        // 用户 2 看不到用户 1 的路线
        mockMvc.perform(get("/route/myList").header("Authorization", auth(2)))
                .andExpect(jsonPath("$.data", hasSize(0)));
    }

    @Test
    void userIdInRequestBodyIsIgnored() throws Exception {
        save(1, ROUTE_JSON.replace("{", "{\"userId\":2,"));
        Long owner = jdbcTemplate.queryForObject("SELECT user_id FROM travel_route", Long.class);
        assertEquals(1L, owner);
    }

    @Test
    void listShowsNewestFirst() throws Exception {
        long first = saveAndGetId(1);
        long second = saveAndGetId(1);
        mockMvc.perform(get("/route/myList").header("Authorization", auth(1)))
                .andExpect(jsonPath("$.data[0].id").value(second))
                .andExpect(jsonPath("$.data[1].id").value(first));
    }

    @Test
    void saveRejectsInvalidInput() throws Exception {
        save(1, ROUTE_JSON.replace("第1天：故宫。\\n第2天：颐和园。", ""))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.msg").value("路线内容不能为空"));
        save(1, ROUTE_JSON.replace("\"days\":3", "\"days\":-1"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.msg").value("出行天数应为1到30之间的整数"));
    }

    @Test
    void deleteOnlyOwnRoute() throws Exception {
        long id = saveAndGetId(1);

        // 用户 2 删除用户 1 的路线：被拒绝，数据仍在
        mockMvc.perform(delete("/route/delete/" + id).header("Authorization", auth(2)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.code").value(400))
                .andExpect(jsonPath("$.msg").value("路线不存在或不可操作"));
        assertEquals(1, jdbcTemplate.queryForObject("SELECT COUNT(*) FROM travel_route", Integer.class));

        mockMvc.perform(delete("/route/delete/" + id).header("Authorization", auth(1)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.msg").value("删除成功"));
        assertEquals(0, jdbcTemplate.queryForObject("SELECT COUNT(*) FROM travel_route", Integer.class));

        // 已删除或不存在
        mockMvc.perform(delete("/route/delete/" + id).header("Authorization", auth(1)))
                .andExpect(status().isBadRequest());
    }

    @Test
    void collectSetsGivenValueAndChecksOwner() throws Exception {
        long id = saveAndGetId(1);

        collect(1, id, "{\"isCollect\":1}").andExpect(status().isOk()).andExpect(jsonPath("$.msg").value("设置成功"));
        collect(1, id, "{\"isCollect\":1}").andExpect(status().isOk());
        assertEquals(1, jdbcTemplate.queryForObject("SELECT is_collect FROM travel_route", Integer.class));

        collect(1, id, "{\"isCollect\":0}").andExpect(status().isOk());
        assertEquals(0, jdbcTemplate.queryForObject("SELECT is_collect FROM travel_route", Integer.class));

        collect(1, id, "{\"isCollect\":2}")
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.msg").value("收藏状态只能为0或1"));

        collect(2, id, "{\"isCollect\":1}")
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.msg").value("路线不存在或不可操作"));
        assertEquals(0, jdbcTemplate.queryForObject("SELECT is_collect FROM travel_route", Integer.class));
    }

    @Test
    void requiresLogin() throws Exception {
        mockMvc.perform(get("/route/myList")).andExpect(status().isUnauthorized());
        mockMvc.perform(delete("/route/delete/1")).andExpect(status().isUnauthorized());
    }
}

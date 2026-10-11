package cn.edu.hebau.aitravel.controller;

import cn.edu.hebau.aitravel.util.JwtUtil;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.test.context.jdbc.Sql;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.ResultActions;

import java.nio.charset.StandardCharsets;
import java.util.Date;

import static org.hamcrest.Matchers.matchesPattern;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

/** 验证注册、登录、BCrypt 保存密码，以及 JWT 对受保护接口的拦截。 */
@SpringBootTest(properties = {
        "spring.datasource.url=jdbc:h2:mem:user-test;MODE=MySQL;DB_CLOSE_DELAY=-1",
        "spring.datasource.username=sa",
        "spring.datasource.password=",
        "jwt.secret=" + UserControllerTests.SECRET
})
@AutoConfigureMockMvc
@Sql("/user-test.sql")
class UserControllerTests {
    static final String SECRET = "test-secret-key-for-unit-tests-only-32chars";

    @Autowired
    private MockMvc mockMvc;
    @Autowired
    private JdbcTemplate jdbcTemplate;
    @Autowired
    private JwtUtil jwtUtil;

    private ResultActions postJson(String url, String json) throws Exception {
        return mockMvc.perform(post(url).contentType(MediaType.APPLICATION_JSON).content(json));
    }

    @Test
    void registerSavesBcryptHashInsteadOfPlainPassword() throws Exception {
        postJson("/user/register", "{\"username\":\"student\",\"password\":\"Example123!\"}")
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(200))
                .andExpect(jsonPath("$.msg").value("注册成功"))
                .andExpect(jsonPath("$.data").doesNotExist());

        String saved = jdbcTemplate.queryForObject(
                "SELECT password FROM `user` WHERE username = 'student'", String.class);
        assertTrue(saved.startsWith("$2a$"), "数据库中应为 BCrypt 散列");
    }

    @Test
    void registerRejectsDuplicateOrEmptyInput() throws Exception {
        postJson("/user/register", "{\"username\":\"student\",\"password\":\"Example123!\"}");

        postJson("/user/register", "{\"username\":\"student\",\"password\":\"Other123!\"}")
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.code").value(400))
                .andExpect(jsonPath("$.msg").value("用户名已存在"));

        postJson("/user/register", "{\"username\":\" \",\"password\":\"Example123!\"}")
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.msg").value("用户名不能为空"));

        postJson("/user/register", "{\"username\":\"other\",\"password\":\"123\"}")
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.msg").value("密码长度应为6到20个字符"));
    }

    @Test
    void loginReturnsTokenAndUserWithoutPassword() throws Exception {
        postJson("/user/register", "{\"username\":\"student\",\"password\":\"Example123!\"}");

        postJson("/user/login", "{\"username\":\"student\",\"password\":\"Example123!\"}")
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(200))
                .andExpect(jsonPath("$.data.token").isString())
                .andExpect(jsonPath("$.data.user.id").isNumber())
                .andExpect(jsonPath("$.data.user.username").value("student"))
                .andExpect(jsonPath("$.data.user.createTime")
                        .value(matchesPattern("\\d{4}-\\d{2}-\\d{2} \\d{2}:\\d{2}:\\d{2}")))
                .andExpect(jsonPath("$.data.user.password").doesNotExist());
    }

    @Test
    void loginWithWrongPasswordOrUnknownUserReturns400() throws Exception {
        postJson("/user/register", "{\"username\":\"student\",\"password\":\"Example123!\"}");

        postJson("/user/login", "{\"username\":\"student\",\"password\":\"wrong-password\"}")
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.code").value(400))
                .andExpect(jsonPath("$.msg").value("用户名或密码错误"));

        postJson("/user/login", "{\"username\":\"nobody\",\"password\":\"Example123!\"}")
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.msg").value("用户名或密码错误"));
    }

    @Test
    void protectedApiRequiresValidToken() throws Exception {
        // 没有令牌
        mockMvc.perform(get("/route/myList"))
                .andExpect(status().isUnauthorized())
                .andExpect(jsonPath("$.code").value(401))
                .andExpect(jsonPath("$.msg").value("未登录或登录已失效"));

        // 令牌被篡改
        String token = jwtUtil.createToken(1L);
        mockMvc.perform(get("/route/myList").header("Authorization", "Bearer " + token + "x"))
                .andExpect(status().isUnauthorized());

        // 令牌已过期
        String expired = Jwts.builder()
                .subject("1")
                .expiration(new Date(System.currentTimeMillis() - 1000))
                .signWith(Keys.hmacShaKeyFor(SECRET.getBytes(StandardCharsets.UTF_8)))
                .compact();
        mockMvc.perform(get("/route/myList").header("Authorization", "Bearer " + expired))
                .andExpect(status().isUnauthorized());

        // 有效令牌能通过登录校验（路线接口在后续阶段实现，此时为 404 而不是 401）
        mockMvc.perform(get("/route/myList").header("Authorization", "Bearer " + token))
                .andExpect(status().isNotFound());
    }
}

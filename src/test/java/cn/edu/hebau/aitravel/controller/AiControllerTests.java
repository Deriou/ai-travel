package cn.edu.hebau.aitravel.controller;

import cn.edu.hebau.aitravel.service.AiService;
import cn.edu.hebau.aitravel.util.AiException;
import cn.edu.hebau.aitravel.util.JwtUtil;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.bean.override.mockito.MockitoSpyBean;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.ResultActions;

import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.contains;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

/** 只替换真正调用大模型的 chat 方法，校验、提示词拼接和异常处理按实际代码运行。 */
@SpringBootTest(properties = {
        "spring.datasource.url=jdbc:h2:mem:ai-test;MODE=MySQL;DB_CLOSE_DELAY=-1",
        "spring.datasource.username=sa",
        "spring.datasource.password=",
        "jwt.secret=test-secret-key-for-unit-tests-only-32chars"
})
@AutoConfigureMockMvc
class AiControllerTests {
    @Autowired
    private MockMvc mockMvc;
    @Autowired
    private JwtUtil jwtUtil;
    @MockitoSpyBean
    private AiService aiService;

    private ResultActions postWithToken(String url, String json) throws Exception {
        return mockMvc.perform(post(url)
                .header("Authorization", "Bearer " + jwtUtil.createToken(1L))
                .contentType(MediaType.APPLICATION_JSON)
                .content(json));
    }

    @Test
    void generateRouteReturnsAiText() throws Exception {
        doReturn("第1天：故宫。\n第2天：颐和园。").when(aiService).chat(anyString());

        postWithToken("/ai/generateRoute", "{\"destination\":\"北京\",\"days\":2,\"preference\":\"休闲、美食\"}")
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(200))
                .andExpect(jsonPath("$.data").value("第1天：故宫。\n第2天：颐和园。"));

        verify(aiService).chat(contains("北京2天"));
    }

    @Test
    void generateTipsReturnsAiText() throws Exception {
        doReturn("出行前查看天气。").when(aiService).chat(anyString());

        postWithToken("/ai/generateTips", "{\"destination\":\"北京\",\"days\":3}")
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data").value("出行前查看天气。"));
    }

    @Test
    void invalidInputIsRejectedWithoutCallingAi() throws Exception {
        postWithToken("/ai/generateRoute", "{\"destination\":\"\",\"days\":3,\"preference\":\"美食\"}")
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.msg").value("目的地不能为空"));

        postWithToken("/ai/generateRoute", "{\"destination\":\"北京\",\"days\":0,\"preference\":\"美食\"}")
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.msg").value("出行天数应为1到30之间的整数"));

        postWithToken("/ai/generateRoute", "{\"destination\":\"北京\",\"days\":3}")
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.msg").value("游玩偏好不能为空"));

        postWithToken("/ai/generateTips", "{\"destination\":\"北京\",\"days\":\"abc\"}")
                .andExpect(status().isBadRequest());

        verify(aiService, never()).chat(anyString());
    }

    @Test
    void aiFailureReturns500() throws Exception {
        doThrow(new AiException("AI生成失败，请稍后重试")).when(aiService).chat(anyString());

        postWithToken("/ai/generateRoute", "{\"destination\":\"北京\",\"days\":3,\"preference\":\"美食\"}")
                .andExpect(status().isInternalServerError())
                .andExpect(jsonPath("$.code").value(500))
                .andExpect(jsonPath("$.msg").value("AI生成失败，请稍后重试"))
                .andExpect(jsonPath("$.data").doesNotExist());
    }

    @Test
    void requiresLogin() throws Exception {
        mockMvc.perform(post("/ai/generateRoute")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"destination\":\"北京\",\"days\":3,\"preference\":\"美食\"}"))
                .andExpect(status().isUnauthorized())
                .andExpect(jsonPath("$.code").value(401));
    }
}

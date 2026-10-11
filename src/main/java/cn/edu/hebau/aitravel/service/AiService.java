package cn.edu.hebau.aitravel.service;

import cn.edu.hebau.aitravel.entity.TravelRoute;
import cn.edu.hebau.aitravel.util.AiException;
import cn.edu.hebau.aitravel.util.BusinessException;
import com.fasterxml.jackson.databind.JsonNode;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.MediaType;
import org.springframework.http.client.SimpleClientHttpRequestFactory;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestClient;

import java.time.Duration;
import java.util.List;
import java.util.Map;

/**
 * AI 业务：校验出行条件，拼接提示词，调用第三方大模型（DeepSeek）得到文本。
 * 接口地址、模型名、超时和 API Key 都来自配置文件。
 */
@Slf4j
@Service
public class AiService {
    private static final String SYSTEM_PROMPT =
            "你是一名旅游规划助手。请用简体中文纯文本回答，不要使用 Markdown 符号（如 #、*、表格）。";

    private final RestClient restClient;
    private final String model;

    public AiService(@Value("${ai.base-url}") String baseUrl,
                     @Value("${ai.model}") String model,
                     @Value("${ai.timeout-seconds}") int timeoutSeconds,
                     @Value("${ai.api-key:}") String apiKey) {
        // 设置连接和读取超时，避免大模型长时间不返回导致请求一直等待。
        SimpleClientHttpRequestFactory factory = new SimpleClientHttpRequestFactory();
        factory.setConnectTimeout(Duration.ofSeconds(10));
        factory.setReadTimeout(Duration.ofSeconds(timeoutSeconds));

        this.restClient = RestClient.builder()
                .baseUrl(baseUrl)
                .defaultHeader("Authorization", "Bearer " + apiKey)
                .requestFactory(factory)
                .build();
        this.model = model;
    }

    /** 生成按天安排的旅游路线。 */
    public String generateRoute(TravelRoute input) {
        checkTrip(input);
        if (input.getPreference() == null || input.getPreference().isBlank()) {
            throw new BusinessException("游玩偏好不能为空");
        }
        if (input.getPreference().length() > 200) {
            throw new BusinessException("游玩偏好不能超过200个字符");
        }
        String prompt = "请为我规划" + input.getDestination() + input.getDays() + "天的旅游路线，"
                + "游玩偏好：" + input.getPreference() + "。"
                + "按“第1天：”“第2天：”的格式每天写一段，包含上午、下午、晚上的安排，语言简洁。";
        return chat(prompt);
    }

    /** 生成交通、天气准备、游玩注意事项等出行小贴士。 */
    public String generateTips(TravelRoute input) {
        checkTrip(input);
        String prompt = "我计划去" + input.getDestination() + "旅游" + input.getDays() + "天。"
                + "请给出出行小贴士，包括交通、天气与穿着准备、游玩注意事项，每条一行，不超过8条。";
        return chat(prompt);
    }

    /** 路线和小贴士共同的校验：目的地必填，天数为 1～30 的整数。 */
    private void checkTrip(TravelRoute input) {
        if (input.getDestination() == null || input.getDestination().isBlank()) {
            throw new BusinessException("目的地不能为空");
        }
        if (input.getDestination().length() > 100) {
            throw new BusinessException("目的地不能超过100个字符");
        }
        if (input.getDays() == null || input.getDays() < 1 || input.getDays() > 30) {
            throw new BusinessException("出行天数应为1到30之间的整数");
        }
    }

    /**
     * 按 OpenAI 兼容格式调用大模型：POST /chat/completions，
     * 返回 JSON 中 choices[0].message.content 就是生成的文本。
     */
    public String chat(String prompt) {
        Map<String, Object> body = Map.of(
                "model", model,
                "messages", List.of(
                        Map.of("role", "system", "content", SYSTEM_PROMPT),
                        Map.of("role", "user", "content", prompt)));
        try {
            JsonNode response = restClient.post()
                    .uri("/chat/completions")
                    .contentType(MediaType.APPLICATION_JSON)
                    .body(body)
                    .retrieve()
                    .body(JsonNode.class);
            String content = response.path("choices").path(0).path("message").path("content").asText();
            if (content.isBlank()) {
                throw new AiException("AI返回内容为空");
            }
            return content.trim();
        } catch (Exception e) {
            // 详细原因写入日志便于排查；返回前端的只有简明提示。
            log.error("调用大模型失败：{}", e.getMessage());
            throw new AiException("AI生成失败，请稍后重试");
        }
    }
}

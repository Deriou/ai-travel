package cn.edu.hebau.aitravel.controller;

import cn.edu.hebau.aitravel.entity.TravelRoute;
import cn.edu.hebau.aitravel.service.AiService;
import cn.edu.hebau.aitravel.util.Result;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/** 接收 AI 生成请求（需要登录）；生成结果直接返回，不自动保存。 */
@RestController
@RequestMapping("/ai")
public class AiController {
    private final AiService aiService;

    public AiController(AiService aiService) {
        this.aiService = aiService;
    }

    /** 请求体：{"destination":"北京","days":3,"preference":"休闲、美食"} */
    @PostMapping("/generateRoute")
    public Result<String> generateRoute(@RequestBody TravelRoute input) {
        return Result.success(aiService.generateRoute(input));
    }

    /** 请求体：{"destination":"北京","days":3} */
    @PostMapping("/generateTips")
    public Result<String> generateTips(@RequestBody TravelRoute input) {
        return Result.success(aiService.generateTips(input));
    }
}

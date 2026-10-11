package cn.edu.hebau.aitravel.controller;

import cn.edu.hebau.aitravel.entity.Scenic;
import cn.edu.hebau.aitravel.service.ScenicService;
import cn.edu.hebau.aitravel.util.PageResult;
import cn.edu.hebau.aitravel.util.Result;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

/** 接收景点分页查询请求；cityId 可不传，页码和每页条数必填。 */
@RestController
@RequestMapping("/scenic")
public class ScenicController {
    private final ScenicService scenicService;

    public ScenicController(ScenicService scenicService) {
        this.scenicService = scenicService;
    }

    @GetMapping("/list")
    public Result<PageResult<Scenic>> list(@RequestParam(required = false) Long cityId,
                                           @RequestParam Integer pageNum,
                                           @RequestParam Integer pageSize) {
        return Result.success(scenicService.page(cityId, pageNum, pageSize));
    }
}

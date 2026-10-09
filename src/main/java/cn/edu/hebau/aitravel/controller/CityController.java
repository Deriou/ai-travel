package cn.edu.hebau.aitravel.controller;

import cn.edu.hebau.aitravel.entity.City;
import cn.edu.hebau.aitravel.service.CityService;
import cn.edu.hebau.aitravel.util.Result;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

/** 接收城市查询请求，并返回 JSON 数据。 */
@RestController
@RequestMapping("/city")
public class CityController {
    private final CityService cityService;

    public CityController(CityService cityService) {
        this.cityService = cityService;
    }

    @GetMapping("/list")
    public Result<List<City>> list() {
        return Result.success(cityService.list());
    }
}

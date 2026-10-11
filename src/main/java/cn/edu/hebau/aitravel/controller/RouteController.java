package cn.edu.hebau.aitravel.controller;

import cn.edu.hebau.aitravel.entity.TravelRoute;
import cn.edu.hebau.aitravel.service.RouteService;
import cn.edu.hebau.aitravel.util.Result;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

/**
 * 个人路线接口，全部需要登录。
 * 参数上的 {@code @AuthenticationPrincipal} 取出 JwtAuthFilter 从令牌中解析的用户编号，不接受前端传入 userId。
 */
@RestController
@RequestMapping("/route")
public class RouteController {
    private final RouteService routeService;

    public RouteController(RouteService routeService) {
        this.routeService = routeService;
    }

    @PostMapping("/save")
    public Result<Long> save(@AuthenticationPrincipal Long userId, @RequestBody TravelRoute route) {
        return Result.success("保存成功", routeService.save(userId, route));
    }

    @GetMapping("/myList")
    public Result<List<TravelRoute>> myList(@AuthenticationPrincipal Long userId) {
        return Result.success(routeService.myList(userId));
    }

    @DeleteMapping("/delete/{id}")
    public Result<Void> delete(@AuthenticationPrincipal Long userId, @PathVariable Long id) {
        routeService.delete(userId, id);
        return Result.success("删除成功", null);
    }

    /** 请求体：{"isCollect":1} 收藏，{"isCollect":0} 取消收藏。 */
    @PutMapping("/collect/{id}")
    public Result<Void> collect(@AuthenticationPrincipal Long userId, @PathVariable Long id,
                                @RequestBody TravelRoute body) {
        routeService.setCollect(userId, id, body.getIsCollect());
        return Result.success("设置成功", null);
    }
}

package cn.edu.hebau.aitravel.service;

import cn.edu.hebau.aitravel.entity.TravelRoute;
import cn.edu.hebau.aitravel.mapper.RouteMapper;
import cn.edu.hebau.aitravel.util.BusinessException;
import org.springframework.stereotype.Service;

import java.util.List;

/** 路线业务：保存、查询、删除、收藏；userId 都来自登录令牌，只能操作自己的路线。 */
@Service
public class RouteService {
    private final RouteMapper routeMapper;

    public RouteService(RouteMapper routeMapper) {
        this.routeMapper = routeMapper;
    }

    /** 保存一条路线，返回新路线的编号。 */
    public Long save(Long userId, TravelRoute route) {
        checkRoute(route);
        route.setUserId(userId);
        routeMapper.insert(route);
        return route.getId();
    }

    public List<TravelRoute> myList(Long userId) {
        return routeMapper.findByUserId(userId);
    }

    public void delete(Long userId, Long id) {
        if (routeMapper.delete(id, userId) == 0) {
            throw new BusinessException("路线不存在或不可操作");
        }
    }

    /** 按指定值设置收藏状态（1 收藏，0 取消），不是取反切换，重复请求结果相同。 */
    public void setCollect(Long userId, Long id, Integer isCollect) {
        if (isCollect == null || (isCollect != 0 && isCollect != 1)) {
            throw new BusinessException("收藏状态只能为0或1");
        }
        if (routeMapper.updateCollect(id, userId, isCollect) == 0) {
            throw new BusinessException("路线不存在或不可操作");
        }
    }

    private void checkRoute(TravelRoute route) {
        if (route.getDestination() == null || route.getDestination().isBlank()) {
            throw new BusinessException("目的地不能为空");
        }
        if (route.getDestination().length() > 100) {
            throw new BusinessException("目的地不能超过100个字符");
        }
        if (route.getDays() == null || route.getDays() < 1 || route.getDays() > 30) {
            throw new BusinessException("出行天数应为1到30之间的整数");
        }
        if (route.getPreference() == null || route.getPreference().isBlank()) {
            throw new BusinessException("游玩偏好不能为空");
        }
        if (route.getPreference().length() > 200) {
            throw new BusinessException("游玩偏好不能超过200个字符");
        }
        if (route.getRouteContent() == null || route.getRouteContent().isBlank()) {
            throw new BusinessException("路线内容不能为空");
        }
    }
}

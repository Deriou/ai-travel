package cn.edu.hebau.aitravel.controller;

import cn.edu.hebau.aitravel.entity.User;
import cn.edu.hebau.aitravel.service.UserService;
import cn.edu.hebau.aitravel.util.Result;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.Map;

/** 接收注册和登录请求；两个接口都允许未登录访问。 */
@RestController
@RequestMapping("/user")
public class UserController {
    private final UserService userService;

    public UserController(UserService userService) {
        this.userService = userService;
    }

    @PostMapping("/register")
    public Result<Void> register(@RequestBody User user) {
        userService.register(user);
        return Result.success("注册成功", null);
    }

    @PostMapping("/login")
    public Result<Map<String, Object>> login(@RequestBody User user) {
        return Result.success(userService.login(user));
    }
}

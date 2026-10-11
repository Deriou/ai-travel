package cn.edu.hebau.aitravel.service;

import cn.edu.hebau.aitravel.entity.User;
import cn.edu.hebau.aitravel.mapper.UserMapper;
import cn.edu.hebau.aitravel.util.BusinessException;
import cn.edu.hebau.aitravel.util.JwtUtil;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.util.Map;

/** 用户业务：注册时检查用户名并保存密码散列，登录时比对密码并签发令牌。 */
@Service
public class UserService {
    private final UserMapper userMapper;
    private final PasswordEncoder passwordEncoder;
    private final JwtUtil jwtUtil;

    public UserService(UserMapper userMapper, PasswordEncoder passwordEncoder, JwtUtil jwtUtil) {
        this.userMapper = userMapper;
        this.passwordEncoder = passwordEncoder;
        this.jwtUtil = jwtUtil;
    }

    public void register(User user) {
        checkInput(user);
        if (userMapper.findByUsername(user.getUsername()) != null) {
            throw new BusinessException("用户名已存在");
        }
        // 数据库只保存 BCrypt 散列，不保存明文密码。
        user.setPassword(passwordEncoder.encode(user.getPassword()));
        userMapper.insert(user);
    }

    /** 返回 token 和用户基本信息；User 的 password 字段不会输出到 JSON。 */
    public Map<String, Object> login(User input) {
        checkInput(input);
        User user = userMapper.findByUsername(input.getUsername());
        // 用户不存在和密码错误给出同一提示，不透露用户名是否已注册。
        if (user == null || !passwordEncoder.matches(input.getPassword(), user.getPassword())) {
            throw new BusinessException("用户名或密码错误");
        }
        String token = jwtUtil.createToken(user.getId());
        return Map.of("token", token, "user", user);
    }

    private void checkInput(User user) {
        if (user.getUsername() == null || user.getUsername().isBlank()) {
            throw new BusinessException("用户名不能为空");
        }
        if (user.getPassword() == null || user.getPassword().isBlank()) {
            throw new BusinessException("密码不能为空");
        }
        user.setUsername(user.getUsername().trim());
        if (user.getUsername().length() > 50) {
            throw new BusinessException("用户名不能超过50个字符");
        }
        if (user.getPassword().length() < 6 || user.getPassword().length() > 20) {
            throw new BusinessException("密码长度应为6到20个字符");
        }
    }
}

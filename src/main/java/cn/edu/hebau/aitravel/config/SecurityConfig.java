package cn.edu.hebau.aitravel.config;

import cn.edu.hebau.aitravel.util.JwtUtil;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;

/** Spring Security 配置：哪些接口公开、哪些需要登录，以及未登录时返回什么。 */
@Configuration
public class SecurityConfig {

    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http, JwtUtil jwtUtil) throws Exception {
        http
                // 前后端分离、使用令牌认证，不需要 CSRF 防护和 Session。
                .csrf(csrf -> csrf.disable())
                .sessionManagement(session -> session.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
                // 注册、登录、城市和景点公开访问，其余接口都需要登录。
                .authorizeHttpRequests(auth -> auth
                        .requestMatchers("/user/register", "/user/login", "/city/**", "/scenic/**", "/error").permitAll()
                        .anyRequest().authenticated())
                // 未登录或令牌无效：HTTP 401，并使用统一的 Result 格式。
                .exceptionHandling(e -> e.authenticationEntryPoint((request, response, ex) -> {
                    response.setStatus(401);
                    response.setContentType("application/json;charset=UTF-8");
                    response.getWriter().write("{\"code\":401,\"msg\":\"未登录或登录已失效\",\"data\":null}");
                }))
                // 在 Spring Security 校验登录状态之前，先由 JWT 过滤器识别用户。
                .addFilterBefore(new JwtAuthFilter(jwtUtil), UsernamePasswordAuthenticationFilter.class);
        return http.build();
    }

    /** BCrypt 密码散列：注册时 encode 加密，登录时 matches 比对。 */
    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }
}

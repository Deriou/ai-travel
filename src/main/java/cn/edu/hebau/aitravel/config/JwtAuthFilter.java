package cn.edu.hebau.aitravel.config;

import cn.edu.hebau.aitravel.util.JwtUtil;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.util.List;

/**
 * 每个请求经过一次：读取 Authorization: Bearer 令牌，校验通过就把用户编号交给 Spring Security。
 * 没有令牌或令牌无效时不做处理，由 SecurityConfig 判断该接口是否允许未登录访问。
 */
public class JwtAuthFilter extends OncePerRequestFilter {
    private final JwtUtil jwtUtil;

    public JwtAuthFilter(JwtUtil jwtUtil) {
        this.jwtUtil = jwtUtil;
    }

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response,
                                    FilterChain chain) throws ServletException, IOException {
        String header = request.getHeader("Authorization");
        if (header != null && header.startsWith("Bearer ")) {
            try {
                Long userId = jwtUtil.parseUserId(header.substring(7));
                // 认证信息中的 principal 就是用户编号，Controller 用 @AuthenticationPrincipal Long userId 取出。
                var authentication = new UsernamePasswordAuthenticationToken(userId, null, List.of());
                SecurityContextHolder.getContext().setAuthentication(authentication);
            } catch (Exception e) {
                // 令牌被篡改、过期或格式错误：保持未登录状态。
                SecurityContextHolder.clearContext();
            }
        }
        chain.doFilter(request, response);
    }
}

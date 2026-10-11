package cn.edu.hebau.aitravel.entity;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;

import java.time.LocalDateTime;

/** 用户表对应的数据，也用于接收注册和登录请求。 */
@Data
public class User {
    private Long id;
    private String username;

    /** 只允许从请求中读取，返回 JSON 时不输出，避免密码散列出现在响应中。 */
    @JsonProperty(access = JsonProperty.Access.WRITE_ONLY)
    private String password;

    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime createTime;
}
